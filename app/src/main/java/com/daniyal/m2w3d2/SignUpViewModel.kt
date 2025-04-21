package com.daniyal.m2w3d2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignUpViewModel : ViewModel() {
//    private val viewState = SignUpViewState()

    private val _viewStateFlow: MutableStateFlow<SignUpViewState> =
        MutableStateFlow(SignUpViewState())
    val viewStateFlow: StateFlow<SignUpViewState> = _viewStateFlow.asStateFlow()

    private val _viewActionFlow: MutableSharedFlow<SignUpViewAction> = MutableSharedFlow()
    val viewActionFlow: SharedFlow<SignUpViewAction> = _viewActionFlow.asSharedFlow()


    private fun dispatcher(signUpAction: SignUpAction) {
        viewModelScope.launch {
            val updatedStateFlow = reduce(signUpAction)

            _viewStateFlow.update {
                updatedStateFlow
            }
        }
    }

    private fun reduce(signUpAction: SignUpAction): SignUpViewState {
        return when (signUpAction) {
            is SignUpAction.Loading -> {
                _viewStateFlow.value.copy(isLoading = signUpAction.isLoading)
            }

            is SignUpAction.Validation -> {
                _viewStateFlow.value.copy(
                    fullName = _viewStateFlow.value.fullName.copy(isError = signUpAction.fullNameHasError),
                    email = _viewStateFlow.value.email.copy(isError = signUpAction.emailHasError),
                    password = _viewStateFlow.value.password.copy(isError = signUpAction.passwordHasError),
                    confirmPassword = _viewStateFlow.value.confirmPassword.copy(isError = signUpAction.conformPasswordHasError),
                )
            }

            is SignUpAction.Update -> {
                _viewStateFlow.value.copy(
                    fullName = _viewStateFlow.value.fullName.copy(value = signUpAction.fullName),
                    email = _viewStateFlow.value.email.copy(value = signUpAction.email),
                    password = _viewStateFlow.value.password.copy(value = signUpAction.password),
                    confirmPassword = _viewStateFlow.value.confirmPassword.copy(value = signUpAction.conformPassword),
                )
            }
        }
    }

    fun handleIntent(signUpIntent: SignUpIntent) {
        when (signUpIntent) {
            is SignUpIntent.UpdateField -> {
                updateSignUpFields(
                    signUpFieldsType = signUpIntent.signUpFieldsType,
                    value = signUpIntent.value
                )
            }

            SignUpIntent.SignUpButtonClicked -> signUpButtonClicked()
        }
    }

    private fun updateSignUpFields(
        signUpFieldsType: SignUpFieldsType,
        value: String
    ) {


        val fieldValue = viewStateFlow.value
        var fullName: String = fieldValue.fullName.value
        var email: String = fieldValue.email.value
        var password: String = fieldValue.password.value
        var confirmPassword: String = fieldValue.confirmPassword.value


        when (signUpFieldsType) {
            SignUpFieldsType.FULL_NAME ->
                fullName = value

            SignUpFieldsType.EMAIL ->
                email = value

            SignUpFieldsType.PASSWORD ->
                password = value

            SignUpFieldsType.CONFORM_PASSWORD ->
                confirmPassword = value
        }
        dispatcher(
            SignUpAction.Update(
                fullName = fullName,
                email = email,
                password = password,
                conformPassword = confirmPassword
            )
        )
    }


    private fun signUpButtonClicked() {
        viewModelScope.launch {
            if (validateSignUpFields()) {
                dispatcher(SignUpAction.Loading(isLoading = true))

                delay(timeMillis = 2000)

                dispatcher(SignUpAction.Loading(isLoading = false))
                _viewActionFlow.emit(SignUpViewAction.ShowToast(message = "Api Successful"))
            }
            else{
                _viewActionFlow.emit(SignUpViewAction.ShowToastError(message = "Api Failed Or may complete the fileds"))

            }

        }
    }

    private fun validateSignUpFields(): Boolean {
        var fullNameHasError: Boolean = false
        var emailHasError: Boolean = false
        var passwordHasError: Boolean = false
        var confirmPasswordHasError: Boolean = false

        if (viewStateFlow.value.fullName.value.isEmpty()) {
            fullNameHasError = true
        }

        if (viewStateFlow.value.email.value.isEmpty()) {
            emailHasError = true
        }

        if (viewStateFlow.value.password.value.isEmpty()) {
            passwordHasError = true
        }

        if (viewStateFlow.value.confirmPassword.value.isEmpty()) {
            confirmPasswordHasError = true
        }

        val signUpAction =
            if (fullNameHasError || emailHasError || passwordHasError || confirmPasswordHasError) {
                SignUpAction.Validation(
                    fullNameHasError,
                    emailHasError,
                    passwordHasError,
                    confirmPasswordHasError
                )
            } else {
                null
            }

        signUpAction?.let {
            dispatcher(it)
        }

        return signUpAction == null
    }
}

