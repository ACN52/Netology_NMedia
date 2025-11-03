package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentAuthBinding
import ru.netology.nmedia.viewmodel.AuthViewModel
import javax.inject.Inject

@AndroidEntryPoint
class AuthFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.signIn.setOnClickListener {
            val login = binding.login.text.toString()
            val password = binding.password.text.toString()

            if (login.isBlank() || password.isBlank()) {
                showError("Please fill all fields")
                return@setOnClickListener
            }

            viewModel.authenticate(login, password)
        }

        // наблюдаем за authProcessState
        viewModel.authProcessState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthViewModel.AuthProcessState.Loading -> {
                    showLoading(true)
                }
                is AuthViewModel.AuthProcessState.Success -> {
                    showLoading(false)
                    // Сохраняем авторизацию
                    appAuth.setAuth(state.authData.id, state.authData.token)
                    // Возвращаемся назад
                    findNavController().popBackStack()
                }
                is AuthViewModel.AuthProcessState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progress.visibility = if (show) View.VISIBLE else View.GONE
        binding.signIn.isEnabled = !show
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}