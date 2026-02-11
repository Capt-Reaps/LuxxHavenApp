package com.example.LuxxHavenApp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import org.json.JSONObject;
import java.util.List;

public class UnitAdapter extends RecyclerView.Adapter<UnitAdapter.UnitViewHolder> {

    private List<JSONObject> unitList;

    public UnitAdapter(List<JSONObject> unitList) {
        this.unitList = unitList;
    }

    @NonNull
    @Override
    public UnitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_unit, parent, false);
        return new UnitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UnitViewHolder holder, int position) {
        try {
            JSONObject unit = unitList.get(position);
            holder.tvName.setText(unit.getString("name"));
            holder.tvPrice.setText("₱" + unit.getString("price_weekday") + " / night");

            String imageUrl = unit.getString("main_image_url");
            Glide.with(holder.itemView.getContext()).load(imageUrl).into(holder.ivUnit);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return unitList.size();
    }

    public static class UnitViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice;
        ImageView ivUnit;

        public UnitViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_unit_name);
            tvPrice = itemView.findViewById(R.id.tv_unit_price);
            ivUnit = itemView.findViewById(R.id.iv_unit_image);
        }
    }
}