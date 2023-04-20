package com.raypazv.bfv2.data.paths

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.raypazv.bfv2.R
import kotlinx.android.synthetic.main.path_item_layout.view.*

class PathAdapter(private var pathList: ArrayList<Path>, var pathClickedListener: OnPathClicked) :
  RecyclerView.Adapter<PathAdapter.PathViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PathAdapter.PathViewHolder {
    val itemView =
      LayoutInflater.from(parent.context).inflate(R.layout.path_item_layout, parent, false)
    return PathViewHolder(itemView)
  }

  override fun onBindViewHolder(holder: PathAdapter.PathViewHolder, position: Int) {
    val currentPath = pathList[position]
    holder.bindView(currentPath, pathClickedListener, position)
  }

  override fun getItemCount(): Int = pathList.size

  inner class PathViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bindView(path: Path, pathClickedListener: OnPathClicked, pathIndex: Int) {
      itemView.pathNameTextView.text = path.name
      itemView.setOnClickListener {
        pathClickedListener.onPathClicked(path.id!!, pathIndex)
      }
    }
  }

  fun submitList(list: ArrayList<Path>) {
    this.pathList = list
  }

  interface OnPathClicked {
    fun onPathClicked(idPath: Int, pathIndex: Int)
  }
}
