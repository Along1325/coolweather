package com.example.lifeassistant.util;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.lifeassistant.R;

import java.util.List;


/**
 * Created by Administrator on 2017/6/12 0012.
 * 自定义适配器
 */

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ViewHolder> {

    private Handler handler;
    private List<MusicName> musicList;

    //创建viewholder实例
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_list,parent,false);//加载布局
        final ViewHolder holder = new ViewHolder(view);//传入布局参数

        holder.musicView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                MusicName musicName = musicList.get(position);

                //向activity发送Message
                Message msg = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("musicName",musicName.getMusicName());
                msg.setData(bundle);
                msg.what = 1;
                handler.sendMessage(msg);
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MusicName mName = musicList.get(position);
        holder.musicName.setText(mName.getMusicName());
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        View musicView;
        TextView musicName;

        public ViewHolder(View itemView) {
            super(itemView);
            musicView = itemView;
            musicName = (TextView) itemView.findViewById(R.id.music_name);
        }
    }

    //把数据传递进来
    public MusicAdapter(List<MusicName> musicList,Handler handler) {
        this.musicList = musicList;
        this.handler = handler;
    }

}
