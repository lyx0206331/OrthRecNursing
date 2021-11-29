package com.chwishay.orthrecnursing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.jetbrains.anko.find

//                       _ooOoo_
//                      o8888888o
//                      88" . "88
//                      (| -_- |)

//                       O\ = /O
//                   ____/`---'\____
//                 .   ' \\| |// `.
//                  / \\||| : |||// \
//                / _||||| -:- |||||- \
//                  | | \\\ - /// | |
//                | \_| ''\---/'' | |
//                 \ .-\__ `-` ___/-. /
//              ______`. .' /--.--\ `. . __
//           ."" '< `.___\_<|>_/___.' >'"".
//          | | : `- \`.;`\ _ /`;.`/ - ` : | |
//            \ \ `-. \_ __\ /__ _/ .-` / /
//    ======`-.____`-.___\_____/___.-`____.-'======
//                       `=---='
//
//    .............................................
//             佛祖保佑             永无BUG
/**
 * author:RanQing
 * date:2021/3/31 0031 9:50
 * description:
 */
class BtDevAdapter(private val connListener: (dev: ClassicBtInfo) -> Unit): ListAdapter<ClassicBtInfo, RecyclerView.ViewHolder>(BtDevDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return DevViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_device_list, parent,false), connListener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as DevViewHolder).bind(getItem(position))
    }

    class DevViewHolder(val view: View, val connListener: (dev: ClassicBtInfo) -> Unit): RecyclerView.ViewHolder(view) {

        fun bind(item: ClassicBtInfo) {
            view.find<AppCompatTextView>(R.id.tvDevName).text = item.dev.name
            view.find<AppCompatTextView>(R.id.tvBond).text = if (item.isBond()) "已绑定" else ""
            view.find<ConstraintLayout>(R.id.clDevItem).onClick {
                connListener(item)
            }
            view.find<AppCompatTextView>(R.id.tvMacAddr).text = item.dev.address
            view.find<AppCompatTextView>(R.id.tvRssi).text = "${item.rssi}dBm"
        }
    }
}

class BtDevDiffCallback: DiffUtil.ItemCallback<ClassicBtInfo>() {
    override fun areItemsTheSame(oldItem: ClassicBtInfo, newItem: ClassicBtInfo): Boolean {
        return oldItem.dev.address == newItem.dev.address
    }

    override fun areContentsTheSame(oldItem: ClassicBtInfo, newItem: ClassicBtInfo): Boolean {
        return oldItem == newItem
    }
}