package com.aviral.apinsta.Utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aviral.apinsta.Models.Comment;
import com.aviral.apinsta.Models.UserAccountSettings;
import com.aviral.apinsta.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentListAdapter extends ArrayAdapter<Comment> {

    private static final String TAG = "AviralKaushik";

    private final LayoutInflater mInflater;
    private final int layoutResource;
    private final Context mContext;

    public CommentListAdapter(@NonNull Context context, int resource, @NonNull List<Comment> objects) {
        super(context, resource, objects);

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        layoutResource = resource;

    }

    private static class ViewHolder {
        TextView comment, username, reply, timestamp, likes;
        CircleImageView profileImage;
        ImageView like;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();

            holder.comment = convertView.findViewById(R.id.comment);
            holder.username = convertView.findViewById(R.id.comment_username);
            holder.timestamp = convertView.findViewById(R.id.comment_time_posted);
            holder.reply = convertView.findViewById(R.id.comment_reply);
            holder.like = convertView.findViewById(R.id.comment_like);
            holder.likes = convertView.findViewById(R.id.comment_likes);
            holder.profileImage = convertView.findViewById(R.id.comment_profile_image);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.comment.setText(getItem(position).getComment());

        // Getting TimeStamp Difference
        String timeStampDifference = getTimeStampDifference(getItem(position));

        if (!timeStampDifference.equals("0")) {
            String timeStamp = timeStampDifference + " d";
            holder.timestamp.setText(timeStamp);
        } else {
            holder.timestamp.setText(mContext.getString(R.string.today));
        }

        // Setting the username and profile photo from firebase
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference
                .child(mContext.getString(R.string.user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot singleSnapshot : snapshot.getChildren()) {

                    holder.username.setText(Objects.requireNonNull(singleSnapshot
                            .getValue(UserAccountSettings.class)).getUsername());

                    ImageLoader imageLoader = ImageLoader.getInstance();

                    imageLoader.displayImage(
                            Objects.requireNonNull(singleSnapshot
                                    .getValue(UserAccountSettings.class)).getProfile_photo(),
                            holder.profileImage
                    );

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: Query Cancelled");
            }
        });

        if (position == 0) {
            holder.like.setVisibility(View.GONE);
            holder.reply.setVisibility(View.GONE);
            holder.likes.setVisibility(View.GONE);
        }


        return convertView;
    }

    private String getTimeStampDifference(Comment comment) {
        Log.d(TAG, "getTimeStampDifference: Getting Time Stamp Difference");

        String difference;

        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));

        Date today = c.getTime();
        sdf.format(today);

        Date timeStamp;
        final String photoTimeStamp = comment.getDate_created();

        try {

            timeStamp = sdf.parse(photoTimeStamp);
            difference = String.valueOf(((today.getTime() - Objects.requireNonNull(timeStamp).getTime()) / 1000 / 60 / 60 / 24));

        } catch (ParseException e) {
            Log.d(TAG, "getTimeStampDifference: ParseException: " + e.getMessage());
            difference = "0";
        }

        return difference;
    }
}
