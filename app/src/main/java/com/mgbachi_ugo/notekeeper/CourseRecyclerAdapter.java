package com.mgbachi_ugo.notekeeper;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class CourseRecyclerAdapter extends RecyclerView.Adapter<CourseRecyclerAdapter.viewHolder>{
   private final Context mContext;
   private final List<CourseInfo> mCourses;
    private final LayoutInflater mLayoutInflater;

    public CourseRecyclerAdapter(Context mContext, List<CourseInfo> mCourses) {
        this.mContext = mContext;
        mLayoutInflater = LayoutInflater.from(mContext);
        this.mCourses = mCourses;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View  itemView = mLayoutInflater.inflate(R.layout.item_course_list, parent, false);
        return new viewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        CourseInfo course = mCourses.get(position);
        holder.mTextCourse.setText(course.getTitle());
        holder.mCurrentPosition = position;

    }

    @Override
    public int getItemCount() {
        return mCourses.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{

        private final TextView mTextCourse;
        public int mCurrentPosition;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            mTextCourse = (TextView) itemView.findViewById(R.id.item_course_view);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Snackbar.make(v, mCourses.get(mCurrentPosition).getTitle(), Snackbar.LENGTH_SHORT).show();
                }
            });
        }
    }
}
