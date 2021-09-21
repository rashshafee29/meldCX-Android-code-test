package com.example.medlcx_android_code_test

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medlcx_android_code_test.adapter.URLHistoryListRecyclerViewAdapter
import com.example.medlcx_android_code_test.model.URLImageInfo
import com.example.medlcx_android_code_test.utils.Constant
import com.orhanobut.hawk.Hawk

class URLHistoryActivity : AppCompatActivity() {

    private lateinit var urlHistoryListRecyclerViewAdapter: URLHistoryListRecyclerViewAdapter
    private lateinit var urlImageInfoList: ArrayList<URLImageInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_urlhistory)
        val recyclerView: RecyclerView = findViewById(R.id.id_recycler_view)
        val layoutManager = LinearLayoutManager(applicationContext)
        recyclerView.layoutManager = layoutManager

        val any: Any? = Hawk.get(Constant.HAWK_TOKEN_KEY)
        if(any == null) {
            urlImageInfoList = ArrayList()
            Hawk.put(Constant.HAWK_TOKEN_KEY, urlImageInfoList)
        } else {
            urlImageInfoList = Hawk.get(Constant.HAWK_TOKEN_KEY)
        }

        urlImageInfoList.reverse() //reversing the list to show latest data at first

        urlHistoryListRecyclerViewAdapter = URLHistoryListRecyclerViewAdapter(this, urlImageInfoList)
        recyclerView.adapter = urlHistoryListRecyclerViewAdapter //setting the adapter to recycler view
    }

    /**
     * Inflate search view on menu bar
     * filtering the list in recycler view with search text
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.search_layout, menu)
        val searchItem: MenuItem = menu!!.findItem(R.id.id_search_button)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                urlHistoryListRecyclerViewAdapter.filter.filter(newText)
                return false
            }
        })
        return true
    }
}