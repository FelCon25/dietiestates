package it.unina.dietiestates.features.property.presentation.addProperty

import android.net.Uri
import it.unina.dietiestates.features.property.domain.Address
import it.unina.dietiestates.features.property.domain.InsertionType
import it.unina.dietiestates.features.property.domain.Property
import it.unina.dietiestates.features.property.domain.PropertyType

data class AddPropertyScreenState(
    val generalInfo: GeneralInfoState = GeneralInfoState(),
    val characteristics: CharacteristicsState = CharacteristicsState(),
    val location: LocationState = LocationState(),
    val currentPage: AddPropertyPage = AddPropertyPage.GENERAL_INFO,
    val isAddingPropertyLoading: Boolean = false,
    val property: Property? = null
)

data class GeneralInfoState(
    val insertionType: InsertionType = InsertionType.SALE,
    val images: List<Uri> = emptyList(),
    val description: String = "",
    val price: String = "",
    val surfaceArea: String = ""
)

data class CharacteristicsState(
    val propertyType: PropertyType = PropertyType.VILLA,
    val rooms: Int? = null,
    val floors: Int? = null,
    val energyClass: String = "A+",
    val airConditioning: Boolean = false,
    val elevator: Boolean = false,
    val concierge: Boolean = false
)

data class LocationState(
    val selectedAddress: Address? = null,
    val searchedAddresses: List<Address> = emptyList(),
    val verifiedAddress: Address? = null,
    val query: String = "",
    val isSearchAddressesLoading: Boolean = false,
    val isVerifyAddressLoading: Boolean = false
)

enum class AddPropertyPage{
    GENERAL_INFO,
    CHARACTERISTICS,
    LOCATION
}