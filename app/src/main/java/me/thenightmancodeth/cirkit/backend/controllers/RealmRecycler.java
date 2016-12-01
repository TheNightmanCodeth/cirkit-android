package me.thenightmancodeth.cirkit.backend.controllers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import me.thenightmancodeth.cirkit.R;
import me.thenightmancodeth.cirkit.backend.models.RealmPush;
import me.thenightmancodeth.cirkit.views.MainActivity;

/**
 * Created by joe on 12/1/16.
 */

public class RealmRecycler extends RealmRecyclerViewAdapter<RealmPush, RealmRecycler.PushListViewHolder> {
    private final MainActivity activity;

    public RealmRecycler(MainActivity act, OrderedRealmCollection<RealmPush> pushes) {
        super(act, pushes, true);
        this.activity = act;
    }

    @Override
    public PushListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new PushListViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(PushListViewHolder holder, int position) {
        RealmPush push = getData().get(position);
        holder.data = push;
        holder.push.setText(push.getMsg());
        //TODO: set received from
    }

    class PushListViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        public TextView push;
        public RealmPush data;

        public PushListViewHolder(View view) {
            super(view);
            push = (TextView)view.findViewById(R.id.list_text);
            view.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View view) {
            activity.itemClick(data);
            return false;
        }
    }
}
