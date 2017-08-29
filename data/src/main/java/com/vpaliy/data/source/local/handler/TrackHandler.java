package com.vpaliy.data.source.local.handler;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import com.vpaliy.data.source.local.utils.DatabaseUtils;
import com.vpaliy.domain.model.MelophileTheme;
import com.vpaliy.domain.model.Track;
import java.util.ArrayList;
import java.util.List;
import android.support.annotation.NonNull;
import javax.inject.Inject;
import javax.inject.Singleton;

import static com.vpaliy.data.source.local.MusicContract.Tracks;
import static com.vpaliy.data.source.local.MusicContract.MelophileThemes;

@SuppressWarnings({"unused","WeakerAccess"})
@Singleton
public class TrackHandler {

    private ContentResolver provider;

    @Inject
    public TrackHandler(@NonNull Context context){
        this.provider=context.getContentResolver();
    }

    public List<Track> queryAll(Query query){
        if(query!=null){
            Cursor cursor=provider.query(Tracks.CONTENT_URI,null,query.selection(),query.args(),null);
            return queryAll(cursor);
        }
        return queryAll();
    }

    private List<Track> queryAll(Cursor cursor){
        if(cursor!=null){
            List<Track> tracks=new ArrayList<>();
            while(cursor.moveToNext()){
                Track track= DatabaseUtils.toTrack(cursor);
                if(track!=null){
                    tracks.add(track);
                }
            }
            if(!cursor.isClosed()) cursor.close();
            return tracks;
        }
        return null;
    }

    public Track query(String id){
        if(!TextUtils.isEmpty(id)){
            Cursor cursor=provider.query(Tracks.buildTrackUri(id),Tracks.COLUMNS,null,null,null);
            if(cursor!=null && cursor.moveToFirst()){
                Track track=DatabaseUtils.toTrack(cursor);
                if(!cursor.isClosed()) cursor.close();
                Log.d(TrackHandler.class.getSimpleName(),"Returning not null");
                return track;
            }
            Log.d(TrackHandler.class.getSimpleName(),"Returning null");
            return null;
        }
        throw new IllegalArgumentException("Id is null");
    }

    public List<Track> queryAll(){
        Cursor cursor=provider.query(Tracks.CONTENT_URI,null,null,null,null);
        return queryAll(cursor);
    }

    public List<Track> queryByTheme(MelophileTheme theme){
        if(theme!=null){
            Cursor cursor=provider.query(MelophileThemes.buildTracksTheme(theme.getTheme()),null,null,null,null);
            if(cursor!=null){
                List<Track> tracks=new ArrayList<>(cursor.getCount());
                Log.d(TrackHandler.class.getSimpleName(),Integer.toString(cursor.getCount()));
                while(cursor.moveToNext()){
                    Track track=query(cursor.getString(cursor.getColumnIndex(MelophileThemes.MELOPHILE_ITEM_ID)));
                    if(track!=null) {
                        tracks.add(track);
                    }
                }
                if(!cursor.isClosed()) cursor.close();
                return tracks;
            }
            return null;
        }
        throw new IllegalArgumentException("Theme is null");
    }

    public void insert(Track track){
        if(track!=null){
            ContentValues values= DatabaseUtils.toValues(track);
            provider.insert(Tracks.CONTENT_URI,values);
        }
    }

    public void insert(MelophileTheme theme, Track track){
        ContentValues values=DatabaseUtils.toValues(theme,track);
        if(values!=null){
            provider.insert(MelophileThemes.buildTracksTheme(),values);
        }
    }
}