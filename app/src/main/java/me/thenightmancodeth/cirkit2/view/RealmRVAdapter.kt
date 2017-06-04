/* 
 * STRIM - A torrent & magnet streamer for Chromecast
 * Copyright (C) 2017 Joseph Diragi (TheNightman)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.thenightmancodeth.cirkit2.view

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.RealmRecyclerViewAdapter
import me.thenightmancodeth.cirkit2.model.RealmPush
import kotlinx.android.synthetic.main.push_item.view.*
import me.thenightmancodeth.cirkit2.R
/**
 * Created by TheNightman on 5/26/17.
 **/

class RealmRVAdapter( pushes: OrderedRealmCollection<RealmPush>, val copyListener: (String) -> Unit, val deleteListener: (RealmPush, RealmRVAdapter) -> Unit) : RealmRecyclerViewAdapter<RealmPush, RealmRVAdapter.PushListViewHolder>(pushes, true) {

    override fun onCreateViewHolder(p0: ViewGroup?, p1: Int): PushListViewHolder{
        val listItem: View = LayoutInflater.from(p0?.context).inflate(R.layout.push_item, p0, false)
        return PushListViewHolder(listItem)
    }

    override fun onBindViewHolder(p0: PushListViewHolder?, p1: Int) {
        val push = data?.get(p1)

        //TODO("Add type field to realmpush")
        val msg: String?
        if (push?.stringMessage != null) {
            msg = push.stringMessage
        } else {
            msg = push?.filePath
        }

        p0?.itemView?.list_text?.text = msg
        p0?.itemView?.sender_list_text?.text = push?.device
        p0?.itemView?.delete?.setOnClickListener { _ ->
            deleteListener(push!!, this)
        }

        p0?.itemView?.copy?.setOnClickListener {
            copyListener(msg!!)
        }
    }

    inline fun Realm.transaction(body: Realm.() -> Unit) {
        beginTransaction()
        body(this)
        commitTransaction()
    }

    class PushListViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
