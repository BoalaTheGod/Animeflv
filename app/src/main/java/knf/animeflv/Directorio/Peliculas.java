package knf.animeflv.Directorio;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import knf.animeflv.AnimeSorter;
import knf.animeflv.JsonFactory.OfflineGetter;
import knf.animeflv.Parser;
import knf.animeflv.R;
import knf.animeflv.Recyclers.AdapterDirPeliculaNew;
import knf.animeflv.Utils.ExecutorManager;

/**
 * Created by Jordy on 30/08/2015.
 */
public class Peliculas extends Fragment {
    RecyclerView rvAnimes;
    View view;
    Parser parser=new Parser();

    public Peliculas() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.directorio_peliculas,container,false);
        rvAnimes = (RecyclerView) view.findViewById(R.id.rv_peliculas);
        rvAnimes.setHasFixedSize(true);
        rvAnimes.setLayoutManager(new LinearLayoutManager(getActivity()));
        setDirectory();
        return view;
    }

    private void setDirectory() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                List<AnimeClass> animes = AnimeSorter.sortByName(parser.DirPelis(getJson()));
                final AdapterDirPeliculaNew adapter = new AdapterDirPeliculaNew(getActivity(), animes);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rvAnimes.setAdapter(adapter);
                    }
                });
                return null;
            }
        }.executeOnExecutor(ExecutorManager.getExecutor());
    }

    public String getJson() {
        return OfflineGetter.getDirectorio();
    }
}
