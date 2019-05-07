package com.rpol.monitor.ui;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rpol.monitor.helpers.BoardItem;
import com.rpol.monitor.R;

import java.util.ArrayList;
import java.util.List;

// Defines how a board is displayed
public class BoardViewAdapter extends  RecyclerView.Adapter<BoardViewAdapter.ViewHolder> {
    private List<BoardItem> boards;

    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView tvBoardName;
        public TextView tvPostNumber;
        public View layout;

        public ViewHolder(View v) {
            super(v);
            layout = v;
            tvBoardName = (TextView) v.findViewById(R.id.tvBoardName);
            tvPostNumber = (TextView) v.findViewById(R.id.tvPostNumber);
        }
    }

    public void add(int position, BoardItem item) {
        boards.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        boards.remove(position);
        notifyItemRemoved(position);
    }

    public BoardViewAdapter(List<BoardItem> items) {
        boards = items;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public BoardViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        // create a new view
        LayoutInflater inflater = LayoutInflater.from(
                parent.getContext());
        View v =
                inflater.inflate(R.layout.board_row_layout, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    // Simply formats a row
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final BoardItem item = boards.get(position);
        String text = "<a href='" + item.getUrl() + "'>" + item.getName() + "</a>";
        holder.tvBoardName.setText(Html.fromHtml(text));
        holder.tvBoardName.setClickable(true);
        holder.tvBoardName.setMovementMethod(LinkMovementMethod.getInstance());
        holder.tvBoardName.setTextColor(Color.BLACK);
        holder.tvBoardName.setTextSize(15);

        holder.tvPostNumber.setText("" + item.getPosts());
        holder.tvPostNumber.setTextColor(item.getColor());
        holder.tvPostNumber.setTextSize(15);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return boards.size();
    }
}
