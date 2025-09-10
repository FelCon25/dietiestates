package it.unina.dietiestates.di

import io.ktor.client.engine.okhttp.OkHttp
import it.unina.dietiestates.core.data.FileReader
import it.unina.dietiestates.core.data.HttpClientFactory
import it.unina.dietiestates.core.data.googleAuth.GoogleAuthUtil
import it.unina.dietiestates.core.data.tokens.TokenManager
import it.unina.dietiestates.core.presentation.MainScreenViewModel
import it.unina.dietiestates.features.admin.data.remote.RemoteAdminDataSource
import it.unina.dietiestates.features.admin.data.remote.RemoteAdminDataSourceImpl
import it.unina.dietiestates.features.admin.data.repository.AdminRepositoryImpl
import it.unina.dietiestates.features.admin.domain.AdminRepository
import it.unina.dietiestates.features.admin.presentation.addAgent.AdminAddAgentScreenViewModel
import it.unina.dietiestates.features.admin.presentation.addAssistant.AdminAddAssistantScreenViewModel
import it.unina.dietiestates.features.admin.presentation.adminScreen.AdminScreenViewModel
import it.unina.dietiestates.features.auth.data.remote.RemoteAuthenticationDataSource
import it.unina.dietiestates.features.auth.data.remote.RemoteAuthenticationDataSourceImpl
import it.unina.dietiestates.features.auth.data.repository.AuthRepositoryImpl
import it.unina.dietiestates.features.auth.domain.AuthRepository
import it.unina.dietiestates.features.auth.presentation.login.SignInScreenViewModel
import it.unina.dietiestates.features.auth.presentation.register.RegisterScreenViewModel
import it.unina.dietiestates.features.profile.data.remote.RemoteProfileDataSource
import it.unina.dietiestates.features.profile.data.remote.RemoteProfileDataSourceImpl
import it.unina.dietiestates.features.profile.data.repository.ProfileRepositoryImpl
import it.unina.dietiestates.features.profile.domain.ProfileRepository
import it.unina.dietiestates.features.profile.presentation.ProfileScreenViewModel
import it.unina.dietiestates.features.property.presentation.bookmarks.BookmarksScreenViewModel
import it.unina.dietiestates.features.property.presentation.home.HomeScreenViewModel
import it.unina.dietiestates.features.property.presentation.savedSearches.SavedSearchesScreenViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val mainModule = module {
    single { TokenManager(context = androidApplication()) }
    single { HttpClientFactory.create(OkHttp.create(), get()) }
    single { GoogleAuthUtil(context = androidApplication()) }
    single { FileReader(androidApplication()) }

    singleOf(::RemoteAuthenticationDataSourceImpl).bind<RemoteAuthenticationDataSource>()
    singleOf(::AuthRepositoryImpl).bind<AuthRepository>()

    singleOf(::RemoteProfileDataSourceImpl).bind<RemoteProfileDataSource>()
    singleOf(::ProfileRepositoryImpl).bind<ProfileRepository>()

    singleOf(::RemoteAdminDataSourceImpl).bind<RemoteAdminDataSource>()
    singleOf(::AdminRepositoryImpl).bind<AdminRepository>()

    viewModelOf(::MainScreenViewModel)
    viewModelOf(::SignInScreenViewModel)
    viewModelOf(::RegisterScreenViewModel)
    viewModelOf(::HomeScreenViewModel)
    viewModelOf(::SavedSearchesScreenViewModel)
    viewModelOf(::BookmarksScreenViewModel)
    viewModelOf(::ProfileScreenViewModel)

    viewModelOf(::AdminScreenViewModel)
    viewModelOf(::AdminAddAssistantScreenViewModel)
    viewModelOf(::AdminAddAgentScreenViewModel)
}