package it.unina.dietiestates.features.auth.presentation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import it.unina.dietiestates.app.Route
import it.unina.dietiestates.core.domain.User
import it.unina.dietiestates.features.auth.presentation.login.SignInScreen
import it.unina.dietiestates.features.auth.presentation.register.RegisterScreen

fun NavGraphBuilder.authGraph(navController: NavHostController, onAuthSucceeded: (User) -> Unit){
    navigation<Route.AuthGraph>(
        startDestination = Route.SignIn
    ){
        composable<Route.SignIn> {
            SignInScreen(
                onNavigateToRegisterScreen = {
                    navController.navigate(Route.Register){
                        popUpTo(Route.SignIn){
                            inclusive = true
                        }
                    }
                },
                onAuthSucceeded = onAuthSucceeded
            )
        }

        composable<Route.Register> {
            RegisterScreen(
                onNavigateToSignInScreen = {
                    navController.navigate(Route.SignIn){
                        popUpTo(Route.Register){
                            inclusive = true
                        }
                    }
                },
                onAuthSucceeded = onAuthSucceeded
            )
        }
    }
}