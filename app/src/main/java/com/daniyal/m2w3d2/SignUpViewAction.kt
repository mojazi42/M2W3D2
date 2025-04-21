package com.daniyal.m2w3d2

sealed class SignUpViewAction {
    data class ShowToast(val message: String) : SignUpViewAction()
    data class ShowToastError(val message: String) : SignUpViewAction()
}