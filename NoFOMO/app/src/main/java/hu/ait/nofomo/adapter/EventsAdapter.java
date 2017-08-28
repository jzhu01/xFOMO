package hu.ait.nofomo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import hu.ait.nofomo.EventsListActivity;
import hu.ait.nofomo.R;
import hu.ait.nofomo.data.Event;


public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder> {

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public TextView tvEventName;
            public TextView tvDesc;

            public ViewHolder(View itemView) {
                super(itemView);
                tvEventName = (TextView) itemView.findViewById(R.id.tvEventName);
                tvDesc = (TextView) itemView.findViewById(R.id.tvDesc);

            }
        }

        private List<Event> eventsList;
        private Context context;
        private int lastPosition = -1;

        public EventsAdapter(List<Event> eventsList, Context context) {
            this.eventsList = eventsList;
            this.context = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.row_event, viewGroup, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }


    @Override
        public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
            viewHolder.tvEventName.setText(eventsList.get(position).getName());
            viewHolder.tvDesc.setText(eventsList.get(position).getDesc());
            setAnimation(viewHolder.itemView, position);
        }

        @Override
        public int getItemCount() {
            return eventsList.size();
        }

        public void removeEvent(int index) {
            ((EventsListActivity) context).deleteEvent(eventsList.get(index));
            eventsList.remove(index);
            notifyItemRemoved(index);
        }

        public void swapPlaces(int oldPosition, int newPosition) {
            if (oldPosition < newPosition) {
                for (int i = oldPosition; i < newPosition; i++) {
                    Collections.swap(eventsList, i, i + 1);
                }
            } else {
                for (int i = oldPosition; i > newPosition; i--) {
                    Collections.swap(eventsList, i, i - 1);
                }
            }
            notifyItemMoved(oldPosition, newPosition);
        }

        public Event getEvent(int i) {
            return eventsList.get(i);
        }

        private void setAnimation(View viewToAnimate, int position) {
            // If the bound view wasn't previously displayed on screen, it's animated
            if (position > lastPosition) {
                Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
                viewToAnimate.startAnimation(animation);
                lastPosition = position;
            }
        }
    }

