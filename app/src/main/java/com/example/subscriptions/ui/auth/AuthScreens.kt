package com.example.subscriptions.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.subscriptions.ui.AppViewModelProvider
import com.example.subscriptions.ui.components.LabeledTextField
import com.example.subscriptions.util.Validators

/** Экран входа */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onOpenRegister: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state = viewModel.state

    AuthLayout(selectedTab = 0, onTabSelected = { if (it == 1) onOpenRegister() }) {
        LabeledTextField(
            label = "Адрес эл. почты",
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            placeholder = "Ваш адрес эл. почты",
            error = state.emailError,
            keyboardType = KeyboardType.Email
        )
        LabeledTextField(
            label = "Пароль",
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            placeholder = "Пароль",
            error = state.passwordError,
            isPassword = true,
            imeAction = ImeAction.Done
        )

        state.generalError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(8.dp))
        PrimaryButton(
            text = "Войти",
            isLoading = state.isLoading,
            onClick = { viewModel.login(onLoginSuccess) }
        )
    }
}

/** Экран регистрации */
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onOpenLogin: () -> Unit,
    viewModel: RegisterViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state = viewModel.state

    AuthLayout(selectedTab = 1, onTabSelected = { if (it == 0) onOpenLogin() }) {
        LabeledTextField(
            label = "Имя",
            value = state.name,
            onValueChange = viewModel::onNameChange,
            placeholder = "Ваше имя",
            error = state.nameError
        )
        LabeledTextField(
            label = "Адрес эл. почты",
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            placeholder = "Ваш адрес эл. почты",
            error = state.emailError,
            keyboardType = KeyboardType.Email
        )
        LabeledTextField(
            label = "Пароль",
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            placeholder = "Пароль",
            error = state.passwordError,
            hint = "Минимум ${Validators.MIN_PASSWORD_LENGTH} символов",
            isPassword = true
        )
        LabeledTextField(
            label = "Повтор пароля",
            value = state.passwordRepeat,
            onValueChange = viewModel::onPasswordRepeatChange,
            placeholder = "Повторите пароль",
            error = state.passwordRepeatError,
            isPassword = true,
            imeAction = ImeAction.Done
        )

        Spacer(Modifier.height(8.dp))
        PrimaryButton(
            text = "Зарегистрироваться",
            isLoading = state.isLoading,
            onClick = { viewModel.register(onRegisterSuccess) }
        )
    }
}

/** Общая разметка экранов входа и регистрации: логотип, вкладки, поля */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthLayout(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(56.dp))

        // Логотип: название и звёздочка
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Мои подписки",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "✦",
                fontSize = 32.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(Modifier.height(8.dp))

        // Переключатель «Вход / Регистрация»
        val tabs = listOf("Вход", "Регистрация")
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { index, title ->
                SegmentedButton(
                    selected = selectedTab == index,
                    onClick = { onTabSelected(index) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = tabs.size)
                ) {
                    Text(title)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        content()
        Spacer(Modifier.height(24.dp))
    }
}

/** Основная кнопка с индикатором загрузки */
@Composable
private fun PrimaryButton(text: String, isLoading: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}
