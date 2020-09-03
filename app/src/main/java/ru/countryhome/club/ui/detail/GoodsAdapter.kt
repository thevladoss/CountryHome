package ru.countryhome.club.ui.detail

import android.widget.TextView
import android.content.Context
import android.graphics.*
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.os.AsyncTask
import android.util.Base64
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import ru.countryhome.club.R
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


internal class GoodsAdapter(context: Context, private val goods: List<Goods>) :
    RecyclerView.Adapter<GoodsAdapter.ViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_goods, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val good = goods[position]
        DownLoadImageTask(holder.img).execute(good.image)
        holder.title.text = good.title
        holder.desc.text = good.description
        holder.cost.text = good.cost
    }

    override fun getItemCount(): Int {
        return goods.size
    }

    inner class ViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        internal val img: ImageView = view.findViewById(R.id.img_good)
        internal val title: TextView = view.findViewById(R.id.text_title_good)
        internal val desc: TextView = view.findViewById(R.id.text_desc_good)
        internal val cost: TextView = view.findViewById(R.id.text_cost_good)

    }


    class DownLoadImageTask(private val imageView: ImageView) : AsyncTask<String, Void, Bitmap?>() {
        private fun sendGet(url: String, username: String, password: String ) : InputStream? {
            val connection = URL(url).openConnection() as HttpURLConnection
            val auth = Base64.encode(("$username:$password").toByteArray(), Base64.DEFAULT).toString(Charsets.UTF_8)
            connection.addRequestProperty("Authorization", "Basic $auth")
            connection.connect()
            return connection.inputStream
        }

        override fun doInBackground(vararg urls: String): Bitmap? {
            var urlOfImage = urls[0]
            urlOfImage = urlOfImage.replace("\\", "")
            return try {
                val inputStream = sendGet(urlOfImage, "admin@irina-galagaeva", "156161561615616")
                BitmapFactory.decodeStream(inputStream)
            } catch (e: Exception) { // Catch the download exception
                e.printStackTrace()
                null
            }
        }
        override fun onPostExecute(result: Bitmap?) {
            if(result!=null){
                // Display the downloaded image into image view
                imageView.setImageBitmap(result)
            }
        }
    }

    private fun sendGet(url: String, username: String, password: String ) : String {
        val connection = URL(url).openConnection() as HttpURLConnection
        val auth = Base64.encode(("$username:$password").toByteArray(), Base64.DEFAULT).toString(Charsets.UTF_8)
        connection.addRequestProperty("Authorization", "Basic $auth")
        connection.connect()
        return connection.inputStream.use { it.reader().use { reader -> reader.readText() } }
    }

}

class Goods (var title: String, var description: String, var image: String, var cost: String)

