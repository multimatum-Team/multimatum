package com.github.multimatum_team.multimatum.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.multimatum_team.multimatum.R
import com.mapbox.search.*
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.ui.view.SearchBottomSheetView


class SearchLocationActivity : AppCompatActivity() {

    private lateinit var searchBottomSheetView: SearchBottomSheetView
    private val searchEngine = MapboxSearchSdk.getSearchEngine()

    private val searchCallback = object : SearchSelectionCallback, SearchMultipleSelectionCallback {
        override fun onCategoryResult(
            suggestion: SearchSuggestion,
            results: List<SearchResult>,
            responseInfo: ResponseInfo,
        ) {
        }

        override fun onError(e: Exception) {
            Toast.makeText(
                applicationContext,
                getString(R.string.location_search_error),
                Toast.LENGTH_SHORT
            ).show()
        }

        override fun onResult(
            suggestions: List<SearchSuggestion>,
            results: List<SearchResult>,
            responseInfo: ResponseInfo,
        ) {
            results.firstOrNull()?.let {
                initializeResultIntent(it)
            }
        }

        override fun onResult(
            suggestion: SearchSuggestion,
            result: SearchResult,
            responseInfo: ResponseInfo,
        ) {
            initializeResultIntent(result)
        }

        override fun onSuggestions(
            suggestions: List<SearchSuggestion>,
            responseInfo: ResponseInfo,
        ) {
            suggestions.firstOrNull()?.let {
                searchEngine.select(suggestions, this)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_location)

        searchBottomSheetView = findViewById(R.id.search_view)
        initializeLocationSearchView(savedInstanceState)
    }

    /**
     * Initialize the location search view with the chosen parameters
     */
    private fun initializeLocationSearchView(savedInstanceState: Bundle?) {
        searchBottomSheetView.initializeSearch(
            savedInstanceState,
            SearchBottomSheetView.Configuration(
                hotCategories = listOf(),
                favoriteTemplates = listOf()
            )
        )

        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_LOCATION
            )
        }

        searchBottomSheetView.visibility = View.VISIBLE
        searchBottomSheetView.isClickable = true
        searchBottomSheetView.isHideableByDrag = false
        searchBottomSheetView.expand()

        // Setting up the listeners
        setLocationSearchViewListeners()
    }

    /**
     * Setup the listeners of the location the search view
     * to handle the user selection
     */
    private fun setLocationSearchViewListeners() {

        // Add a listener for an eventual place selection
        searchBottomSheetView.addOnHistoryClickListener { historyRecord ->
            val historyRecordIntent = Intent()
            historyRecordIntent.putExtra("name", historyRecord.name)
            historyRecordIntent.putExtra("latitude", historyRecord.coordinate!!.latitude())
            historyRecordIntent.putExtra("longitude", historyRecord.coordinate!!.longitude())
            setResult(RESULT_OK, historyRecordIntent)
            finish()
        }
        // Add a listener for an eventual place selection in the history
        searchBottomSheetView.addOnSearchResultClickListener { result, _ ->
            initializeResultIntent(result)
        }

        searchBottomSheetView.addOnCategoryClickListener { category ->
            searchEngine.search(
                category.geocodingCanonicalName,
                SearchOptions(),
                searchCallback
            )
        }
    }

    /**
     * Constructs the the return intent with the parameters that are
     * necessary for the deadline creation
     */
    private fun initializeResultIntent(it: SearchResult) {
        val resultIntent = Intent()
        resultIntent.putExtra("name", it.name)
        resultIntent.putExtra("latitude", it.coordinate!!.latitude())
        resultIntent.putExtra("longitude", it.coordinate!!.longitude())
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private companion object {
        private const val PERMISSIONS_REQUEST_LOCATION = 0

        fun Context.isPermissionGranted(permission: String): Boolean {
            return ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}