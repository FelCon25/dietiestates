package it.unina.dietiestates.features.auth.presentation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import androidx.navigation.navigation
import it.unina.dietiestates.app.Route
import it.unina.dietiestates.core.domain.User
import it.unina.dietiestates.features.auth.presentation.forgotPassword.ForgotPasswordScreen
import it.unina.dietiestates.features.auth.presentation.login.SignInScreen
import it.unina.dietiestates.features.auth.presentation.register.RegisterScreen
import it.unina.dietiestates.features.auth.presentation.resetPassword.ResetPasswordScreen

fun NavGraphBuilder.authGraph(navController: NavHostController, onAuthSucceeded: (User) -> Unit){
    navigation<Route.AuthGraph>(
        startDestination = Route.SignIn(passwordReset = false)
    ){
        composable<Route.SignIn> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.SignIn>()
            SignInScreen(
                showPasswordResetSuccess = route.passwordReset,
                onNavigateToRegisterScreen = {
                    navController.navigate(Route.Register){
                        popUpTo(Route.SignIn(passwordReset = false)){
                            inclusive = true
                        }
                    }
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Route.ForgotPassword)
                },
                onAuthSucceeded = onAuthSucceeded
            )
        }

        composable<Route.Register> {
            RegisterScreen(
                onNavigateToSignInScreen = {
                    navController.navigate(Route.SignIn(passwordReset = false)){
                        popUpTo(Route.Register){
                            inclusive = true
                        }
                    }
                },
                onAuthSucceeded = onAuthSucceeded
            )
        }

        composable<Route.ForgotPassword> {
            ForgotPasswordScreen(
                onBackNavigation = {
                    navController.navigateUp()
                },
                onCodeSent = {
                    navController.navigate(Route.ResetPassword) {
                        popUpTo(Route.ForgotPassword) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable<Route.ResetPassword> {
            ResetPasswordScreen(
                onBackNavigation = {
                    navController.navigateUp()
                },
                onPasswordReset = {
                    navController.navigate(Route.SignIn(passwordReset = true)) {
                        popUpTo(Route.AuthGraph) {
                            inclusive = false
                        }
                    }
                }
            )
        }
    }
}