package me.thenightmancodeth.cirkit.backend.controllers;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import me.thenightmancodeth.cirkit.R;
import me.thenightmancodeth.cirkit.backend.models.RealmPush;
import me.thenightmancodeth.cirkit.views.MainActivity;

import static android.content.Context.CLIPBOARD_SERVICE;

/***************************************
 * Created by TheNightman on 12/1/16   *
 ***************************************/

public class RealmRecycler extends RealmRecyclerViewAdapter<RealmPush, RealmRecycler.PushListViewHolder> {
    private MainActivity act;

    public RealmRecycler(MainActivity act, OrderedRealmCollection<RealmPush> pushes) {
        super(act, pushes, true);
        this.act = act;
    }

    @Override
    public PushListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new PushListViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(PushListViewHolder holder, int position) {
        final RealmPush push = getData().get(position);
        holder.push.setText(push.getMsg());
        holder.sender.setText(push.getSender());
        holder.copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Realm r = Realm.getDefaultInstance();
                r.beginTransaction();
                push.deleteFromRealm();
                r.commitTransaction();
            }
        });
        holder.deleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager cb = (ClipboardManager) act.getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(act.getString(R.string.app_name), push.getMsg());
                cb.setPrimaryClip(clip);
                act.makeSnackBar("Copied to clipboard!");
            }
        });
    }

    class PushListViewHolder extends RecyclerView.ViewHolder {
        TextView push;
        TextView sender;
        ImageButton copyButton;
        ImageButton deleButton;

        PushListViewHolder(View view) {
            super(view);
            push = (TextView)view.findViewById(R.id.list_text);
            sender = (TextView) view.findViewById(R.id.sender_list_text);
            copyButton = (ImageButton) view.findViewById(R.id.copy);
            deleButton = (ImageButton) view.findViewById(R.id.delete);
        }
    }
}
