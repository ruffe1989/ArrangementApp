package no.usn.grupp1.arrangementapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;


// Connects gridview with seat selection activity

public class SeatAdapter extends BaseAdapter{

    private int[] seatId;

    public SeatAdapter(int[] seatId, Context context) {
        this.seatId = seatId;
        this.context = context;
    }

    private Context context;

    @Override
    public int getCount() {
        return seatId.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View seats;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null){
            seats = new View(context);
            seats = inflater.inflate(R.layout.simple_seat, null);
            ImageView imageView = seats.findViewById(R.id.seatImage);

            if(seatId[position] == -1){
                Glide.with(context).load(R.drawable.opptattsete).into(imageView);
            }else{
                Glide.with(context).load(R.drawable.ledigsete).into(imageView);
            }
        }
        else {
            seats = convertView;
        }
        return seats;
    }
}
