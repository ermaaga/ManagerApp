package it.uniba.di.sms2021.managerapp.utility;

import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.chip.Chip;

import it.uniba.di.sms2021.managerapp.R;

public class SearchUtil {
    /**
     * Inizializza la barra di ricerca e gli eventuali filtri se presenti.
     * Affinchè funzioni, searchFilters deve avere la struttura ViewGroup/ViewGroup/Chip dove al
     * posto di Chip possono esserci un numero illimitato di Chip o anche nessuno.
     * Quando si seleziona un chip verrà aggiunta la sua etichetta alla query che quindi dovrà
     * essere gestita nell' OnSearchListener
     * @param context il contesto attuale
     * @param searchView la barra di ricerca da inizializzare
     * @param searchFilters i filtri da inizializzare, può avere valore null
     * @param hintRes la risorsa del testo di aiuto
     * @param onSearchListener listener che specifica l'azione da compiere nella ricerca
     */
    public static void setUpSearchBar(Context context, SearchView searchView,
                                      @Nullable ViewGroup searchFilters, @StringRes int hintRes,
                                      OnSearchListener onSearchListener) {
        searchView.setIconifiedByDefault(true);
        searchView.setQueryHint(context.getString(hintRes));
        searchView.setInputType(InputType.TYPE_CLASS_TEXT);

        // Fa apparire i filtri per la ricerca
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchFilters != null) {
                    searchFilters.setVisibility(View.VISIBLE);
                }
            }
        });

        // Fa scomparire i filtri per la ricerca
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                if (searchFilters != null) {
                    searchFilters.setVisibility(View.GONE);
                }
                return false;
            }
        });

        //Quando un utente digita qualcosa nella barra, il fragment decide che azioni prendere.
        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                onSearchListener.onSearchAction(query);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                onSearchListener.onSearchAction(newText);

                return false;
            }
        };
        searchView.setOnQueryTextListener(queryTextListener);

        if (searchFilters != null) {
            // Al click di qualunque dei filtri, viene aggiunta alla query il filtro corrispondente
            ConstraintLayout searchFilterLayout = (ConstraintLayout) searchFilters.getChildAt(0);
            for (int i = 0; i < searchFilterLayout.getChildCount(); i++) {
                Chip chip = (Chip) searchFilterLayout.getChildAt(i);
                chip.setOnClickListener(v -> {
                    appendToSearchQuery(searchView, chip.getText().toString());
                });
            }
        }
    }

    /**
     * Aggiunge un nuovo termine di ricerca alla query della barra di ricerca
     */
    public static void appendToSearchQuery (SearchView searchView, String string) {
        String query = searchView.getQuery().toString();

        // Aggiunge uno spazio alla query se già non finisce con uno
        if (!query.matches(" $")) {
            query += " ";
        }

        query += string;

        searchView.setQuery(query, true);
    }

    /**
     * Listener che specifica l'azione da eseguire quando si ricerca un'elemento nella schermata
     */
    public interface OnSearchListener {
        void onSearchAction(String query);
    }
}
