package com6510.dcs.shef.ac.uk;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.util.List;

import com6510.dcs.shef.ac.uk.gallery.R;

public class BrowseAdapter extends RecyclerView.Adapter<BrowseAdapter.View_Holder> {
    private Context context;
    private List<ImageElement> items;

    public BrowseAdapter(List<ImageElement> items) {
        this.items = items;
    }

    public BrowseAdapter(Context cont, List<ImageElement> items) {
        super();
        this.items = items;
        context = cont;
    }

    @Override
    public View_Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflate the layout, initialize the View Holder
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.browse_item,
                parent, false);
        View_Holder holder = new View_Holder(v);
        context= parent.getContext();
        return holder;
    }

    @Override
    public void onBindViewHolder(final View_Holder holder, final int position) {
        /* Use the provided View Holder on the onCreateViewHolder method to populate the
           current row on the RecyclerView */
        if (holder!=null && items.get(position)!=null) {
            File f = new File(items.get(position).getImPath());
            Bitmap myBitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
            holder.imageView.setImageBitmap(myBitmap);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ShowImageActivity.class);
                    intent.putExtra("position", position);
                    context.startActivity(intent);
                }
            });
        }
        //animate(holder);
    }

    ImageElement getItem(int id) {
        return items.get(id);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<ImageElement> getItems() {
        return items;
    }

    public void setItems(List<ImageElement> items) {
        this.items = items;
    }

    public class View_Holder extends RecyclerView.ViewHolder  {
        ImageView imageView;

        View_Holder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.image_item);
        }
    }
}