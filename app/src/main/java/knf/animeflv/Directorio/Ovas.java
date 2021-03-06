package knf.animeflv.Directorio;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import knf.animeflv.Directorio.DB.DirectoryHelper;
import knf.animeflv.Parser;
import knf.animeflv.R;
import knf.animeflv.Recyclers.AdapterDirPeliculaNew;
import knf.animeflv.Utils.DesignUtils;
import knf.animeflv.Utils.ExecutorManager;
import knf.animeflv.Utils.Keys;
import knf.animeflv.Utils.ThemeUtils;

/**
 * Created by Jordy on 30/08/2015.
 */
public class Ovas extends Fragment {
    RecyclerView rvAnimes;
    View view;
    Parser parser=new Parser();

    public Ovas() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.directorio_ovas,container,false);
        ThemeUtils.Theme theme = ThemeUtils.Theme.create(getActivity());
        view.setBackgroundColor(ThemeUtils.isTablet(getActivity()) ? theme.primary : theme.background);
        rvAnimes = view.findViewById(R.id.rv_ovas);
        rvAnimes.setHasFixedSize(true);
        rvAnimes.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        if (ThemeUtils.isTablet(getActivity()) && !DesignUtils.forcePhone(getActivity())) {
            rvAnimes.setPadding(0, (int) Parser.toPx(getActivity(), 10), 0, Keys.getNavBarSize(getActivity()));
            rvAnimes.setClipToPadding(false);
        }
        setDirectory(getActivity());
        Log.e(this.getClass().getName(), "Created!!!");
        return view;
    }

    public void setDirectory(final Activity context) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    List<AnimeClass> animes = DirectoryHelper.get(context).getAllType("OVA");
                    final AdapterDirPeliculaNew adapter = new AdapterDirPeliculaNew(context, animes);
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            rvAnimes.setAdapter(adapter);
                        }
                    });
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.executeOnExecutor(ExecutorManager.getExecutor());
    }
}
