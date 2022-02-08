package com.niekam.smartmeter.base

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.niekam.smartmeter.R
import com.niekam.smartmeter.base.errors.mapErrorToStringResource


abstract class BaseFragment<T : BaseViewModel> : Fragment() {
    private val itemsObserver = Observer<Throwable?> { err ->
        err?.let {
            showErrorSnackBar(it)
        }
    }

    abstract val viewModel: T

    fun styleActionBar(title: String = getString(R.string.defaultTitle), showBackButton: Boolean) {
        (activity as AppCompatActivity).supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(showBackButton)
            it.title = title
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.let {
            it.errors.observe(this, itemsObserver)
            it.onAttached()
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.let {
            it.errors.removeObserver(itemsObserver)
            it.onDetached()
        }
    }

    private fun showErrorSnackBar(err: Throwable) {
        Snackbar.make(requireView(), err.mapErrorToStringResource(), Snackbar.LENGTH_LONG).apply {
            view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.md_red_400))
        }.show()
    }
}