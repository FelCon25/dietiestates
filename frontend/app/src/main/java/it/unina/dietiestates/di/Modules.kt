package it.unina.dietiestates.di

import io.ktor.client.engine.okhttp.OkHttp
import it.unina.dietiestates.core.data.FileReader
import it.unina.dietiestates.core.data.HttpClientFactory
import it.unina.dietiestates.core.data.googleAuth.GoogleAuthUtil
import it.unina.dietiestates.core.data.location.LocationDataSource
import it.unina.dietiestates.core.data.location.LocationDataSourceImpl
import it.unina.dietiestates.core.data.location.LocationRepositoryImpl
import it.unina.dietiestates.core.data.tokens.TokenManager
import it.unina.dietiestates.core.domain.location.LocationRepository
import it.unina.dietiestates.core.presentation.MainScreenViewModel
import it.unina.dietiestates.features.agency.data.remote.RemoteAgencyDataSource
import it.unina.dietiestates.features.agency.data.remote.RemoteAgencyDataSourceImpl
import it.unina.dietiestates.features.agency.data.repository.AgencyRepositoryImpl
import it.unina.dietiestates.features.agency.domain.AgencyRepository
import it.unina.dietiestates.features.agency.presentation.addAgent.AddAgentScreenViewModel
import it.unina.dietiestates.features.agency.presentation.addAssistant.AddAssistantScreenViewModel
import it.unina.dietiestates.features.agency.presentation.adminScreen.AdminScreenViewModel
import it.unina.dietiestates.features.agency.presentation.agentScreen.AgentScreenViewModel
import it.unina.dietiestates.features.agency.presentation.assistantScreen.AssistantScreenViewModel
import it.unina.dietiestates.features.auth.data.remote.RemoteAuthenticationDataSource
import it.unina.dietiestates.features.auth.data.remote.RemoteAuthenticationDataSourceImpl
import it.unina.dietiestates.features.auth.data.repository.AuthRepositoryImpl
import it.unina.dietiestates.features.auth.domain.AuthRepository
import it.unina.dietiestates.features.auth.presentation.forgotPassword.ForgotPasswordViewModel
import it.unina.dietiestates.features.auth.presentation.login.SignInScreenViewModel
import it.unina.dietiestates.features.auth.presentation.register.RegisterScreenViewModel
import it.unina.dietiestates.features.auth.presentation.resetPassword.ResetPasswordViewModel
import it.unina.dietiestates.features.profile.data.remote.RemoteProfileDataSource
import it.unina.dietiestates.features.profile.data.remote.RemoteProfileDataSourceImpl
import it.unina.dietiestates.features.profile.data.repository.ProfileRepositoryImpl
import it.unina.dietiestates.features.profile.domain.ProfileRepository
import it.unina.dietiestates.features.profile.presentation.ProfileScreenViewModel
import it.unina.dietiestates.features.profile.presentation.changePassword.ChangePasswordViewModel
import it.unina.dietiestates.features.property.data.remote.RemoteGeocodeDataSource
import it.unina.dietiestates.features.property.data.remote.RemoteGeocodeDataSourceImpl
import it.unina.dietiestates.features.property.data.remote.RemotePropertyDataSource
import it.unina.dietiestates.features.property.data.remote.RemotePropertyDataSourceImpl
import it.unina.dietiestates.features.property.data.repository.GeocodeRepositoryImpl
import it.unina.dietiestates.features.property.data.repository.PropertyRepositoryImpl
import it.unina.dietiestates.features.property.domain.GeocodeRepository
import it.unina.dietiestates.features.property.domain.PropertyRepository
import it.unina.dietiestates.features.property.presentation.addProperty.AddPropertyScreenViewModel
import it.unina.dietiestates.features.property.presentation.bookmarks.BookmarksScreenViewModel
import it.unina.dietiestates.features.property.presentation.home.HomeScreenViewModel
import it.unina.dietiestates.features.property.presentation.savedSearches.SavedSearchesScreenViewModel
import it.unina.dietiestates.features.property.presentation.search.SearchScreenViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import it.unina.dietiestates.features.property.presentation.drawSearch.DrawSearchScreenViewModel
import it.unina.dietiestates.features.property.presentation.propertyDetails.PropertyDetailsScreenViewModel

val mainModule = module {
    single { TokenManager(context = androidApplication()) }
    single { HttpClientFactory.create(OkHttp.create(), get()) }
    single { GoogleAuthUtil(context = androidApplication()) }
    single { FileReader(androidApplication()) }

    singleOf(::LocationDataSourceImpl).bind<LocationDataSource>()
    singleOf(::LocationRepositoryImpl).bind<LocationRepository>()

    singleOf(::RemoteAuthenticationDataSourceImpl).bind<RemoteAuthenticationDataSource>()
    singleOf(::AuthRepositoryImpl).bind<AuthRepository>()

    singleOf(::RemoteProfileDataSourceImpl).bind<RemoteProfileDataSource>()
    singleOf(::ProfileRepositoryImpl).bind<ProfileRepository>()

    singleOf(::RemoteAgencyDataSourceImpl).bind<RemoteAgencyDataSource>()
    singleOf(::AgencyRepositoryImpl).bind<AgencyRepository>()

    singleOf(::RemoteGeocodeDataSourceImpl).bind<RemoteGeocodeDataSource>()
    singleOf(::GeocodeRepositoryImpl).bind<GeocodeRepository>()

    singleOf(::RemotePropertyDataSourceImpl).bind<RemotePropertyDataSource>()
    singleOf(::PropertyRepositoryImpl).bind<PropertyRepository>()

    viewModelOf(::MainScreenViewModel)
    viewModelOf(::SignInScreenViewModel)
    viewModelOf(::RegisterScreenViewModel)
    viewModelOf(::ForgotPasswordViewModel)
    viewModelOf(::ResetPasswordViewModel)
    viewModelOf(::HomeScreenViewModel)
    viewModelOf(::SavedSearchesScreenViewModel)
    viewModelOf(::BookmarksScreenViewModel)
    viewModelOf(::ProfileScreenViewModel)
    viewModelOf(::ChangePasswordViewModel)

    viewModelOf(::AdminScreenViewModel)
    viewModelOf(::AddAssistantScreenViewModel)
    viewModelOf(::AddAgentScreenViewModel)

    viewModelOf(::AssistantScreenViewModel)

    viewModelOf(::AgentScreenViewModel)
    viewModelOf(::AddPropertyScreenViewModel)
    viewModelOf(::DrawSearchScreenViewModel)
    viewModelOf(::SearchScreenViewModel)
    viewModelOf(::PropertyDetailsScreenViewModel)

}