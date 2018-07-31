package no.usn.grupp1.arrangementapp;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ArrAdapter extends RecyclerView.Adapter<ArrAdapter.ViewHolder> {

    private ArrayList<Arrangement> mArrData;
    private Context mContext;
    private SessionManager session;

    public ArrAdapter(Context mContext, ArrayList<Arrangement>  mArrData ) {
        this.mArrData = mArrData;
        this.mContext = mContext;
        session = new SessionManager(mContext);
    }

    @Override
    public ArrAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_arr, parent, false));
    }

    @Override
    public void onBindViewHolder(ArrAdapter.ViewHolder holder, int position) {
        //Get current event
        Arrangement currentArr = mArrData.get(position);

        //Populate arractivity
        holder.bindTo(currentArr);

        // landscape mode
        if(mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            Glide.with(mContext)
                    .load("http://kingbingfuji.synology.me/bilderApp/picture" + (currentArr.getPos() +1))
                    .override(600,600)
                    .centerCrop()
                    .into(holder.mArrImage);
        }
        // portrait mode
        if(mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            Glide.with(mContext)
                    //.load(currentArr.getImageResource())
                    .load("http://kingbingfuji.synology.me/bilderApp/picture" + (currentArr.getPos() +1))
                    .override(600,400)
                    .centerCrop()
                    .into(holder.mArrImage);
        }

    }

    @Override
    public int getItemCount() {return mArrData.size();}

    class ViewHolder extends RecyclerView.ViewHolder{

        //Member Variables for the TextViews
        private TextView mTitleText;
        private TextView mInfoText;
        private TextView mDateText;
        private TextView mTimeText;
        private TextView mAgeText;
        private TextView mFeeText;
        private ImageView mArrImage;

        /**
         * Constructor for the ViewHolder, used in onCreateViewHolder().
         * @param itemView The rootview of the list_item.xml layout file
         */
        ViewHolder(View itemView) {
            super(itemView);
            //Initialize the views
            mTitleText = itemView.findViewById(R.id.title);
            mInfoText = itemView.findViewById(R.id.comment);
            mDateText = itemView.findViewById(R.id.date);
            mTimeText = itemView.findViewById(R.id.time);
            mAgeText = itemView.findViewById(R.id.age);
            mFeeText = itemView.findViewById(R.id.fee);
            mArrImage = itemView.findViewById(R.id.arrImage);

            //Create ticket button
            Button ticket = itemView.findViewById(R.id.ticketbtn);
            ticket.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Arrangement currentArr = mArrData.get(getAdapterPosition());
                    Log.d("Title", currentArr.getTittel());
                    Intent detailIntent = new Intent(mContext, LoadSeatData.class);
                    detailIntent.putExtra("title", currentArr.getTittel());
                    detailIntent.putExtra("eventID", currentArr.getEventID());
                    mContext.startActivity(detailIntent);
                }
            });

        }

        void bindTo(Arrangement currentArr){
            //Populate the textviews with data
            mTitleText.setText(currentArr.getTittel());
            mInfoText.setText(currentArr.getDescription());
            mDateText.setText(currentArr.getDate());
            mTimeText.setText(currentArr.getTime());
            mAgeText.setText(currentArr.getAge());
            mFeeText.setText(currentArr.getFee());

        }


    }
}
