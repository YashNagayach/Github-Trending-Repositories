package com.yash.trending.ui.repos

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.yash.trending.R
import com.yash.trending.data.model.Repo
import com.yash.trending.databinding.FragmentReposBinding
import com.yash.trending.ui.repos.adapter.ReposAdapter
import com.yash.trending.ui.repos.adapter.ReposLoadStateAdapter
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ReposFragment : Fragment(R.layout.fragment_repos), ReposAdapter.ItemClickListener {

    private val viewModel by viewModels<ReposViewModel>()
    private var lastFirstVisiblePosition: Int = 0
    private var _binding: FragmentReposBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)

        _binding = FragmentReposBinding.bind(view)

        val adapter = ReposAdapter(this)

        binding.apply {

            recycler.apply {
                setHasFixedSize(true)
                itemAnimator = null
                this.adapter = adapter.withLoadStateHeaderAndFooter(
                    header = ReposLoadStateAdapter { adapter.retry() },
                    footer = ReposLoadStateAdapter { adapter.retry() }
                )
                postponeEnterTransition()
                viewTreeObserver.addOnPreDrawListener {
                    startPostponedEnterTransition()
                    true
                }
            }

            btnRetry.setOnClickListener {
                adapter.retry()
            }
        }

        viewModel.repos.observe(viewLifecycleOwner) {
            adapter.submitData(viewLifecycleOwner.lifecycle, it)
        }

        adapter.addLoadStateListener { loadState ->
            binding.apply {
                progress.isVisible = loadState.source.refresh is LoadState.Loading
                recycler.isVisible = loadState.source.refresh is LoadState.NotLoading
                btnRetry.isVisible = loadState.source.refresh is LoadState.Error
                error.isVisible = loadState.source.refresh is LoadState.Error

                // no results found
                if (loadState.source.refresh is LoadState.NotLoading &&
                    loadState.append.endOfPaginationReached &&
                    adapter.itemCount < 1
                ) {
                    recycler.isVisible = false
                    emptyTv.isVisible = true
                } else {
                    emptyTv.isVisible = false
                }
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClickListener(item: Repo, position: Int) {
        val itemBackground: ColorDrawable =
            binding.recycler[position].background as ColorDrawable
        if (itemBackground.color == ContextCompat.getColor(
                activity?.applicationContext!!,
                R.color.white
            )
        ) {
            binding.recycler.children.iterator().forEach { item ->
                item.setBackgroundColor(
                    ContextCompat.getColor(
                        activity?.applicationContext!!,
                        R.color.white
                    )
                )
            }
            binding.recycler[position].setBackgroundColor(
                ContextCompat.getColor(activity?.applicationContext!!, R.color.colorBackground)
            )
        } else {
            binding.recycler.children.iterator().forEach { item ->
                item.setBackgroundColor(
                    ContextCompat.getColor(
                        activity?.applicationContext!!,
                        R.color.white
                    )
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        binding.recycler.layoutManager?.onSaveInstanceState()
            ?.let { viewModel.saveRecyclerViewState(it) }
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.stateInitialized()) {
            binding.recycler.layoutManager?.onRestoreInstanceState(
                viewModel.restoreRecyclerViewState()
            )
        }
    }
}