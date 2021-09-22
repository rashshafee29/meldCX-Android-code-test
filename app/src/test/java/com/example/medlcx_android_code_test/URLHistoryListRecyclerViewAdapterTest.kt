package com.example.medlcx_android_code_test

import android.app.Application
import android.content.Context
import com.example.medlcx_android_code_test.model.URLImageInfo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import androidx.recyclerview.widget.RecyclerView.ViewHolder

import androidx.constraintlayout.widget.ConstraintLayout
import com.example.medlcx_android_code_test.adapter.URLHistoryListRecyclerViewAdapter
import junit.framework.Assert.*
import org.robolectric.annotation.Config
import java.util.*
import kotlin.collections.ArrayList

@RunWith(RobolectricTestRunner::class)
@Config(manifest=Config.NONE, sdk = [30])
class URLHistoryListRecyclerViewAdapterTest {
    private lateinit var context: Context
    private lateinit var urlImageInfoList: ArrayList<URLImageInfo>

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        val application: Application = RuntimeEnvironment.application
        assertNotNull(application)
        context = application
        urlImageInfoList = createTestValues()
    }


    @Test
    fun test_onCreateViewHolder() {
        val adapter = URLHistoryListRecyclerViewAdapter(context, urlImageInfoList)
        val parent = ConstraintLayout(context)

        val childViewHolder: ViewHolder =
            adapter.onCreateViewHolder(parent, 1)
        assertTrue(childViewHolder is URLHistoryListRecyclerViewAdapter.URLHistoryListViewHolder)
    }

    @Test
    fun test_getItemCount() {
        val adapter = URLHistoryListRecyclerViewAdapter(context, urlImageInfoList)

        val initialExpected = 5
        val initialActual: Int = adapter.itemCount
        assertEquals(initialExpected, initialActual)
    }

    //Creating 5 test items
    private fun createTestValues() : ArrayList<URLImageInfo> {
        val list: ArrayList<URLImageInfo> = ArrayList()
        for (i in 1..5) {
            val urlImageInfo = URLImageInfo("TestUrl.com", Date(), "TestName")
            list.add(urlImageInfo)
        }
        return list
    }
}