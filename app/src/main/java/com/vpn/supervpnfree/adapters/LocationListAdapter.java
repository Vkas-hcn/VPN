package com.vpn.supervpnfree.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.vpn.supervpnfree.Preference;
import com.vpn.supervpnfree.R;
import com.vpn.supervpnfree.activities.ServerActivity;
import com.vpn.supervpnfree.data.Hot;
import com.vpn.supervpnfree.data.KeyAppFun;
import com.vpn.supervpnfree.data.ServiceData;
import java.util.List;



public class LocationListAdapter extends RecyclerView.Adapter<LocationListAdapter.ViewHolder> {

    public ServerActivity context;
    private Preference preference;

    private List<ServiceData> serviceDataList;
    public LocationListAdapter( ServerActivity cntec,List<ServiceData> serviceDataList) {
        this.context = cntec;
        preference = new Preference(this.context);
        this.serviceDataList = serviceDataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.server_list_free, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        ServiceData bean = serviceDataList.get(position);
        if (position == 0) {
            holder.flag.setImageResource(context.getResources().getIdentifier("drawable/earthspeed", null, context.getPackageName()));
            holder.app_name.setText(R.string.best_performance_server);
            holder.limit.setVisibility(View.INVISIBLE);
        } else {
            holder.flag.setImageResource(KeyAppFun.INSTANCE.getFlagImageData(bean.getWIqcDNWy()));
            holder.app_name.setText(bean.getWIqcDNWy());
            holder.limit.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(view -> {
            Hot.INSTANCE.chooeServices(context,preference,bean);
        });
    }

    @Override
    public int getItemCount() {
        return serviceDataList != null ? serviceDataList.size() : 0;
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView app_name;
        ImageView flag, pro;
        ImageView limit;

        ViewHolder(View v) {
            super(v);
            this.app_name = itemView.findViewById(R.id.region_title);
            this.limit = itemView.findViewById(R.id.region_limit);
            this.flag = itemView.findViewById(R.id.country_flag);
            this.pro = itemView.findViewById(R.id.pro);
        }
    }


}
