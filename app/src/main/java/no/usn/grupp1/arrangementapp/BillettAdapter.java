package no.usn.grupp1.arrangementapp;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class BillettAdapter extends RecyclerView.Adapter<BillettAdapter.ViewHolder>{

    //Member variables
    private ArrayList<Billett> mBillettData;
    private Context mContext;

    private int selectedPos = 0;


    public BillettAdapter(Context mContext, ArrayList<Billett>  mBillettData) {
        this.mBillettData = mBillettData;
        this.mContext = mContext;
    }



    @Override
    public BillettAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_billett, parent, false));
    }

    @Override
    public void onBindViewHolder(no.usn.grupp1.arrangementapp.BillettAdapter.ViewHolder holder, int position) {

        //Get current event
        Billett currentBillett = mBillettData.get(position);
        //Populate the textviews with data
        holder.bindTo(currentBillett);


    }

    @Override
    public int getItemCount() {
        return mBillettData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        //Member Variables for the TextViews
        private TextView mTittel;
        private TextView mSeat;
        private TextView mFee;
        private Button deletebtn;


        /**
         * Constructor for the ViewHolder, used in onCreateViewHolder().
         * @param itemView The rootview of the list_item.xml layout file
         */
        ViewHolder(View itemView) {
            super(itemView);

            //Initialize the views
            mTittel = itemView.findViewById(R.id.arrNameBillett);
            mSeat = itemView.findViewById(R.id.seatBillett);
            mFee = itemView.findViewById(R.id.feeBillett);
        }



        void bindTo(final Billett currentBillett){
            //Populate the textviews with data
            // Endrer kanskje til strings.xml-verdier.
            mTittel.setText(currentBillett.getTittel());
            mSeat.setText("Sete: " + currentBillett.getSeat() + "");
            mFee.setText("Pris: " + currentBillett.getFee() + "kr");




        }


    }

}


