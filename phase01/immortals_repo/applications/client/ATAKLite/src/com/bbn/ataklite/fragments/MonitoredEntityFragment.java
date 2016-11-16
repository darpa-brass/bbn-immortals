package com.bbn.ataklite.fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import com.bbn.ataklite.entities.EntityChangeListener;
import com.bbn.ataklite.entities.MonitoredEntityArrayAdapter;
import com.bbn.ataklite.entities.MonitoredEntityManager;

import javax.annotation.Nonnull;

/**
 * A fragment representing a list of Items.
 * <p>
 * <p>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class MonitoredEntityFragment extends ListFragment implements EntityChangeListener {

    private OnFragmentInteractionListener mListener;

    private MonitoredEntityManager entityManager;

    private MonitoredEntityArrayAdapter listAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MonitoredEntityFragment() {
    }

    public void setEntityManager(MonitoredEntityManager entityManager) {
        this.entityManager = entityManager;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        listAdapter = new MonitoredEntityArrayAdapter(getActivity(), entityManager.getEntityList());
        setListAdapter(listAdapter);

        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // Currently not listening to this
//        super.onListItemClick(l, v, position, id);

        // Currently not listening to this
//        if (null != mListener) {
        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
//            mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
//        }
    }

    @Override
    public void onMyLocationChanged(@Nonnull Location newLocation) {
    }

    @Override
    public void onExternalEntityLocationAddedOrChanged(@Nonnull String identifier, @Nonnull Location newLocation) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listAdapter.notifyDataSetChanged();
            }
        });

    }

    @Override
    public void onExternalEntityImageAdded(@Nonnull String identifier, @Nonnull Location imageLocation, @Nonnull String imageUrl) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

}
