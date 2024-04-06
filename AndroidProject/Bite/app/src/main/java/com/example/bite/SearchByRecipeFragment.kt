package com.example.bite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class SearchByRecipeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search_by_recipe, container, false)
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as SearchActivity).updateTitle("Search recipe")
    }
}