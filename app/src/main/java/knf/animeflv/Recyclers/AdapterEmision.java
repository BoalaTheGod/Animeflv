package knf.animeflv.Recyclers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import knf.animeflv.Emision.Section.TimeCompareModel;
import knf.animeflv.Parser;
import knf.animeflv.PicassoCache;
import knf.animeflv.R;
import knf.animeflv.info.InfoNew;

/**
 * Created by Jordy on 22/08/2015.
 */
public class AdapterEmision extends RecyclerView.Adapter<AdapterEmision.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView iv_rel;
        public TextView tv_tit;
        public TextView tv_time;
        public TextView tv_no;
        public CardView card;
        public LinearLayout normal;
        public LinearLayout lnull;

        public ViewHolder(View itemView) {
            super(itemView);
            this.iv_rel = (ImageView) itemView.findViewById(R.id.imgCardEmision);
            this.tv_tit = (TextView) itemView.findViewById(R.id.tv_emision_tit);
            this.tv_time = (TextView) itemView.findViewById(R.id.tv_emision_time);
            this.tv_no = (TextView) itemView.findViewById(R.id.emision_no);
            this.normal = (LinearLayout) itemView.findViewById(R.id.emision_normal);
            this.lnull = (LinearLayout) itemView.findViewById(R.id.emision_null);
            this.card = (CardView) itemView.findViewById(R.id.cardRel);
        }
    }

    private Context context;
    List<TimeCompareModel> Animes;

    public AdapterEmision(Context context, List<TimeCompareModel> animes) {
        this.context = context;
        this.Animes = animes;
    }

    @Override
    public AdapterEmision.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).
                inflate(R.layout.item_anime_emision, parent, false);
        return new AdapterEmision.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final AdapterEmision.ViewHolder holder, final int position) {
        if (!Animes.get(holder.getAdapterPosition()).getAid().equals("-1")) {
            PicassoCache.getPicassoInstance(context).load(Animes.get(holder.getAdapterPosition()).getImage()).error(R.drawable.ic_block_r).into(holder.iv_rel);
            holder.card.setCardBackgroundColor(context.getResources().getColor(R.color.blanco));
            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("is_amoled", false)) {
                holder.card.setCardBackgroundColor(Color.parseColor("#212121"));
                holder.tv_tit.setTextColor(context.getResources().getColor(R.color.blanco));
                holder.tv_time.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            }
            holder.tv_tit.setText(Animes.get(holder.getAdapterPosition()).getTitulo());
            holder.tv_time.setText(UTCtoLocal(Animes.get(holder.getAdapterPosition()).getTime()));
            holder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putString("aid", Animes.get(holder.getAdapterPosition()).getAid());
                    bundle.putString("link", new Parser().getUrlAnimeCached(Animes.get(holder.getAdapterPosition()).getAid()));
                    Intent intent = new Intent(context, InfoNew.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtras(bundle);
                    //SharedPreferences.Editor sharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE).edit();
                    //sharedPreferences.putString("aid", Animes.get(holder.getAdapterPosition()).getAid()).apply();
                    context.startActivity(intent);
                }
            });
            Boolean resaltar = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("resaltar", true);
            String favoritos = context.getSharedPreferences("data", Context.MODE_PRIVATE).getString("favoritos", "");
            String aid = Animes.get(holder.getAdapterPosition()).getAid();
            Boolean comp = favoritos.startsWith(aid + ":::") || favoritos.contains(":::" + aid + ":::") || favoritos.endsWith(":::" + aid);
            if (comp) {
                if (resaltar)
                    holder.card.setCardBackgroundColor(Color.argb(100, 26, 206, 246));
            }
        } else {
            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("is_amoled", false)) {
                holder.card.setCardBackgroundColor(Color.parseColor("#212121"));
                holder.tv_no.setTextColor(context.getResources().getColor(R.color.blanco));
            }
            holder.normal.setVisibility(View.GONE);
            holder.lnull.setVisibility(View.VISIBLE);
        }
    }

    private String UTCtoLocal(String utc) {
        String convert = "";
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("~hh:mmaa", Locale.ENGLISH);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date myDate = simpleDateFormat.parse(utc);
            simpleDateFormat.setTimeZone(TimeZone.getDefault());
            convert = simpleDateFormat.format(myDate);
        } catch (Exception e) {
            e.printStackTrace();
            convert = utc + "-UTC--->" + e.getMessage();
        }
        return convert;
    }

    @Override
    public int getItemCount() {
        return Animes.size();
    }
}
