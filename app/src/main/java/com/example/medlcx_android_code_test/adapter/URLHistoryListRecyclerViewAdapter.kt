package com.example.medlcx_android_code_test.adapter

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.medlcx_android_code_test.R
import com.example.medlcx_android_code_test.model.URLImageInfo
import android.provider.MediaStore

import android.content.ContentUris
import android.content.Intent

import android.net.Uri

import android.os.Build
import android.provider.MediaStore.VOLUME_EXTERNAL_PRIMARY
import android.widget.Filter
import android.widget.Filterable
import com.example.medlcx_android_code_test.URLHistoryActivity
import com.example.medlcx_android_code_test.utils.Constant
import com.example.medlcx_android_code_test.utils.MethodUtils
import com.orhanobut.hawk.Hawk
import java.util.*
import kotlin.collections.ArrayList

class URLHistoryListRecyclerViewAdapter(private var context: Context, private var urlImageInfoList: ArrayList<URLImageInfo>)
    : RecyclerView.Adapter<URLHistoryListRecyclerViewAdapter.URLHistoryListViewHolder>(), Filterable{

    private var fullURLImageInfoList: ArrayList<URLImageInfo> = ArrayList(urlImageInfoList)

    class URLHistoryListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val urlTextView: TextView = itemView.findViewById(R.id.id_url_text_view)
        val dateTextView: TextView = itemView.findViewById(R.id.id_date_text_view)
        val deleteBtn: ImageView = itemView.findViewById(R.id.id_delete)
        val imageHolder: ImageView = itemView.findViewById(R.id.id_image_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): URLHistoryListViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.base_item, parent, false)
        return URLHistoryListViewHolder(view)
    }

    private fun sendDataBack(position: Int) {
        val returnIntent = Intent()
        returnIntent.putExtra(Constant.RETURN_URL, urlImageInfoList[position].url)
        returnIntent.putExtra(Constant.RETURN_IMAGE_NAME, urlImageInfoList[position].imageName)
        (context as Activity).setResult(Activity.RESULT_OK, returnIntent)
        (context as Activity).finish()
    }

    override fun onBindViewHolder(holder: URLHistoryListViewHolder, position: Int) {
        holder.urlTextView.text = urlImageInfoList[position].url
        holder.urlTextView.setOnClickListener {
            sendDataBack(position)
        }
        val date = urlImageInfoList[position].date
        val dateTime = DateFormat.format("MMMM dd, yyyy hh:mm a", date)
        holder.dateTextView.text = dateTime
        holder.imageHolder.setImageURI(null)
        val uri = MethodUtils.getImageFromMediaStore(context, urlImageInfoList[position].imageName)
        holder.imageHolder.setImageURI(uri)
        holder.imageHolder.setOnClickListener {
            sendDataBack(position)
        }

        holder.deleteBtn.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Do you want to delete this item?")
                .setPositiveButton("YES") { dialogInterface, _ ->
                    deleteAndUpdateDB(position)
                    dialogInterface.dismiss()
                }.setNegativeButton("NO") { dialogInterface,_ ->
                    dialogInterface.dismiss()
                }.show()
        }
    }

    private fun deleteAndUpdateDB(position: Int) {
        urlImageInfoList.remove(urlImageInfoList[position])
        urlImageInfoList.reverse()
        Hawk.put(Constant.HAWK_TOKEN_KEY, urlImageInfoList)
        urlImageInfoList.reverse()
        notifyItemRemoved(position)
    }

    override fun getItemCount(): Int {
        return urlImageInfoList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val searchString = constraint.toString()
                val searchURLList = ArrayList<URLImageInfo>()

                if (constraint == null || constraint.isEmpty()) {
                    searchURLList.addAll(fullURLImageInfoList)
                } else {
                    for(urlImageInfo in fullURLImageInfoList) {
                        if (urlImageInfo.url.lowercase(Locale.ROOT).contains(searchString.lowercase(
                                Locale.ROOT))) {
                            searchURLList.add(urlImageInfo)
                        }
                    }
                }

                val filterResults = FilterResults()
                filterResults.values = searchURLList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                urlImageInfoList.clear()
                urlImageInfoList.addAll(results?.values as ArrayList<URLImageInfo>)
                notifyDataSetChanged()
            }
        }
    }
}