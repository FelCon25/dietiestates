package it.unina.dietiestates.features.property.presentation.addProperty

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unina.dietiestates.core.domain.onError
import it.unina.dietiestates.core.domain.onLoading
import it.unina.dietiestates.core.domain.onSuccess
import it.unina.dietiestates.features.agency.domain.getEmptyAgency
import it.unina.dietiestates.features.property.domain.Address
import it.unina.dietiestates.features.property.domain.GeocodeRepository
import it.unina.dietiestates.features.property.domain.InsertionType
import it.unina.dietiestates.features.property.domain.Property
import it.unina.dietiestates.features.property.domain.PropertyCondition
import it.unina.dietiestates.features.property.domain.PropertyRepository
import it.unina.dietiestates.features.property.domain.PropertyType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddPropertyScreenViewModel(
    private val geocodeRepository: GeocodeRepository,
    private val propertyRepository: PropertyRepository
): ViewModel() {

    private val _eventsChannel = Channel<AddPropertyScreenEvent>()
    val eventsChannelFlow = _eventsChannel.receiveAsFlow()

    private val _state = MutableStateFlow(AddPropertyScreenState())
    val state = _state.asStateFlow()

    val energyClassOptions = listOf("A+", "A", "B", "C", "D", "E", "F", "G")
    val roomsOptions = (1..50).toList()
    val floorsOptions = (1..10).toList()


    fun onEvent(event: AddPropertyScreenEvent){
        when(event){
            is AddPropertyScreenEvent.OnNavigateToNextPageRequested -> {
                when(_state.value.currentPage){
                    AddPropertyPage.GENERAL_INFO -> checkGeneralInfo()
                    AddPropertyPage.CHARACTERISTICS -> checkCharacteristics()
                    AddPropertyPage.LOCATION -> checkLocation()
                }
            }

            is AddPropertyScreenEvent.OnNavigateToNextPage -> {
                viewModelScope.launch {
                    _state.update {
                        it.copy(
                            currentPage = AddPropertyPage.entries[
                                (it.currentPage.ordinal + 1).coerceIn(AddPropertyPage.entries.indices)
                            ]
                        )
                    }
                    _eventsChannel.send(event)
                }
            }

            is AddPropertyScreenEvent.OnNavigateToPrevPage -> {
                viewModelScope.launch {
                    _state.update {
                        it.copy(
                            currentPage = AddPropertyPage.entries[
                                (it.currentPage.ordinal - 1).coerceIn(AddPropertyPage.entries.indices)
                            ]
                        )
                    }
                    _eventsChannel.send(event)
                }
            }

            is AddPropertyScreenEvent.OnWrongValueInput -> {
                viewModelScope.launch {
                    _eventsChannel.send(event)
                }
            }

            is AddPropertyScreenEvent.OnAddProperty -> {
                _state.update {
                    it.copy(
                        property = Property(
                            propertyId = 0,
                            agencyId = 0,
                            description = it.generalInfo.description,
                            price = it.generalInfo.price.toDouble(),
                            surfaceArea = it.generalInfo.surfaceArea.toInt(),
                            rooms = it.characteristics.rooms ?: 0,
                            floors = it.characteristics.floors ?: 0,
                            energyClass = it.characteristics.energyClass,
                            concierge = it.characteristics.concierge,
                            airConditioning = it.characteristics.airConditioning,
                            furnished = false,
                            elevator = it.characteristics.elevator,
                            propertyType = it.characteristics.propertyType,
                            insertionType = it.generalInfo.insertionType,
                            address = "${it.location.verifiedAddress?.route} ${it.location.verifiedAddress?.streetNumber}",
                            city = it.location.verifiedAddress?.city.toString(),
                            postalCode = it.location.verifiedAddress?.postalCode.toString(),
                            province = it.location.verifiedAddress?.province.toString(),
                            country = "Italy",
                            latitude = it.location.verifiedAddress?.latitude ?: 0.0,
                            longitude = it.location.verifiedAddress?.longitude ?: 0.0,
                            images = emptyList(),
                            agency = getEmptyAgency(),
                            agentId = 0,
                            propertyCondition = PropertyCondition.GOOD_CONDITION,
                            createdAt = ""
                        )
                    )
                }
                addProperty()
            }

            is AddPropertyScreenEvent.OnPropertyAddedSuccessfully -> {
                viewModelScope.launch {
                    _eventsChannel.send(event)
                }
            }

            is AddPropertyScreenEvent.OnPropertyAddingFailed -> {
                viewModelScope.launch {
                    _eventsChannel.send(event)
                }
            }
        }
    }

    fun searchAddresses(){
        viewModelScope.launch {

            _state.value.location.query.let {  query ->
                if(query.isBlank()){
                    return@launch
                }

                geocodeRepository.getAddressesBySearch(query).collect { result ->

                    result.apply {
                        onSuccess { addresses ->
                            _state.update {
                                it.copy(
                                    location = it.location.copy(
                                        searchedAddresses = addresses
                                    )
                                )
                            }
                        }

                        onError {
                            println("Unable to retrieve addresses")
                            _state.update {
                                it.copy(
                                    location = it.location.copy(
                                        searchedAddresses = emptyList()
                                    )
                                )
                            }
                        }

                        onLoading {  isLoading ->
                            _state.update {
                                it.copy(
                                    location = it.location.copy(
                                        isSearchAddressesLoading = isLoading
                                    )
                                )
                            }
                        }
                    }
                }
            }

        }
    }

    fun verifyAddress(){
        _state.value.location.selectedAddress?.let { address ->
            viewModelScope.launch {
                geocodeRepository.verifyAddress(address).collect { result ->
                    result.apply {
                        onSuccess { addressVerified ->
                            _state.update {
                                it.copy(
                                    location = it.location.copy(
                                        verifiedAddress = addressVerified
                                    )
                                )
                            }
                            println("Address verified: $addressVerified")
                        }

                        onError {

                        }

                        onLoading { isLoading ->
                            _state.update {
                                it.copy(
                                    location = it.location.copy(
                                        isVerifyAddressLoading = isLoading
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun addProperty(){
        _state.value.property?.let { property ->
            viewModelScope.launch {
                val images = _state.value.generalInfo.images

                propertyRepository.createProperty(property, images).collect { result ->
                    result.apply {
                        onSuccess { property ->
                            onEvent(AddPropertyScreenEvent.OnPropertyAddedSuccessfully(property))
                        }

                        onError {
                            _state.update {
                                it.copy(
                                    property = null
                                )
                            }
                            onEvent(AddPropertyScreenEvent.OnPropertyAddingFailed)
                        }

                        onLoading { isLoading ->
                            _state.update {
                                it.copy(
                                    isAddingPropertyLoading = isLoading
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun onInputQueryChanged(query: String){
        _state.update {
            it.copy(
                location = it.location.copy(query = query)
            )
        }
    }

    fun onInsertionTypeSelected(insertionType: InsertionType){
        _state.update { it ->
            it.copy(
                generalInfo = it.generalInfo.copy(insertionType = insertionType)
            )
        }
    }

    fun onDescriptionInputChanged(description: String){
        _state.update {
            it.copy(
                generalInfo = it.generalInfo.copy(description = description)
            )
        }
    }

    fun addImageSelected(images: List<Uri>){
        _state.update {
            it.copy(
                generalInfo = it.generalInfo.copy(images = it.generalInfo.images + images)
            )
        }
    }

    fun onRemoveImage(atIndex: Int){
        _state.update {
            it.copy(
                generalInfo = it.generalInfo.copy(images = it.generalInfo.images.toMutableList().apply { removeAt(atIndex) })
            )
        }
    }

    fun onSurfaceAreaInputChanged(surfaceArea: String){
        _state.update {
            it.copy(
                generalInfo = it.generalInfo.copy(surfaceArea = surfaceArea)
            )
        }
    }

    fun onPriceInputChanged(price: String){
        _state.update {
            it.copy(
                generalInfo = it.generalInfo.copy(price = price)
            )
        }
    }

    fun onPropertyTypeSelected(propertyType: PropertyType){
        _state.update {
            it.copy(
                characteristics = it.characteristics.copy(propertyType = propertyType)
            )
        }
    }

    fun onRoomsInputChanged(rooms: Int){
        _state.update {
            it.copy(
                characteristics = it.characteristics.copy(rooms = rooms)
            )
        }
    }

    fun onFloorsInputChanged(floors: Int){
        _state.update {
            it.copy(
                characteristics = it.characteristics.copy(floors = floors)
            )
        }
    }

    fun onEnergyClassSelected(energyClass: String){
        _state.update {
            it.copy(
                characteristics = it.characteristics.copy(energyClass = energyClass)
            )
        }
    }

    fun onElevatorStateChanged(elevator: Boolean){
        _state.update {
            it.copy(
                characteristics = it.characteristics.copy(elevator = elevator)
            )
        }
    }

    fun onAirConditioningStateChanged(airConditioning: Boolean){
        _state.update {
            it.copy(
                characteristics = it.characteristics.copy(airConditioning = airConditioning)
            )
        }
    }

    fun onConciergeStateChanged(concierge: Boolean){
        _state.update {
            it.copy(
                characteristics = it.characteristics.copy(concierge = concierge)
            )
        }
    }

    fun onAddressSelected(address: Address){
        _state.update {
            it.copy(
                location = it.location.copy(
                    selectedAddress = address
                )
            )
        }
    }

    fun clearVerifiedAddress(){
        _state.update {
            it.copy(
                location = it.location.copy(
                    verifiedAddress = null
                )
            )
        }
    }

    private fun checkGeneralInfo(){
        _state.value.generalInfo.let { generalInfoState ->
            if(generalInfoState.description.isEmpty()){
                onEvent(AddPropertyScreenEvent.OnWrongValueInput("Description can't be empty"))
            }
            else if(generalInfoState.images.isEmpty()){
                onEvent(AddPropertyScreenEvent.OnWrongValueInput("Select at least one property image"))
            }
            else if(generalInfoState.surfaceArea.toIntOrNull() == null){
                onEvent(AddPropertyScreenEvent.OnWrongValueInput("Please insert a valid surface area"))
            }
            else if(generalInfoState.price.toDoubleOrNull() == null){
                onEvent(AddPropertyScreenEvent.OnWrongValueInput("Please insert a valid price"))
            }
            else{
                onEvent(AddPropertyScreenEvent.OnNavigateToNextPage)
            }
        }
    }

    private fun checkCharacteristics(){
        _state.value.characteristics.let { characteristicsState ->
            if(characteristicsState.rooms == null){
                onEvent(AddPropertyScreenEvent.OnWrongValueInput("Please insert a valid number of rooms"))
            }
            else if(characteristicsState.floors == null){
                onEvent(AddPropertyScreenEvent.OnWrongValueInput("Please insert a valid number of floors"))
            }
            else{
                onEvent(AddPropertyScreenEvent.OnNavigateToNextPage)
            }
        }
    }

    private fun checkLocation(){
        _state.value.location.let {
            if(it.selectedAddress == null){
                onEvent(AddPropertyScreenEvent.OnWrongValueInput("Please select the property location"))
            }
            else{
                onEvent(AddPropertyScreenEvent.OnAddProperty)
            }
        }
    }
}