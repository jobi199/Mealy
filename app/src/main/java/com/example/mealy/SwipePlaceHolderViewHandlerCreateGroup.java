package com.example.mealy;

import android.content.Context;
import android.util.Log;

import com.mindorks.placeholderview.SwipeDecor;
import com.mindorks.placeholderview.SwipePlaceHolderView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class SwipePlaceHolderViewHandlerCreateGroup {

    public List<Integer> mStackIDs;
    public List<Object> mResolvers;

    private Context mContext;

    public SwipePlaceHolderViewHandlerCreateGroup(Context mContext) {
        this.mContext = mContext;
    }

    public void loadSwipePlaceholderView(List<Integer> selectedIndices, List<Recipe> allRecipes, SwipePlaceHolderView swipePlaceHolderView) {
        swipePlaceHolderView.removeAllViews();
        if (!(selectedIndices.size() == 0)) {
            mStackIDs = new ArrayList<>();
            mResolvers = new ArrayList<>();
            for(int i = 0; i < selectedIndices.size(); i++){
                if (!ActivityCreateGroup.mLikedIDs.contains(selectedIndices.get(i)) && !ActivityCreateGroup.mDislikedIDs.contains(selectedIndices.get(i))) {
                    try {
                        swipePlaceHolderView.addView(new RecipeCardHandlerCreateGroup(mContext, allRecipes.get(selectedIndices.get(i)), swipePlaceHolderView));
                        mStackIDs.add(selectedIndices.get(i));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            mResolvers = swipePlaceHolderView.getAllResolvers();
        }
    }

    public void setSwipePlaceHolderViewBuilder(SwipePlaceHolderView swipePlaceHolderView) {
        swipePlaceHolderView.getBuilder()
                .setDisplayViewCount(3)
                .setIsUndoEnabled(true)
                .setSwipeDecor(new SwipeDecor()
                        .setRelativeScale(0.01f)
                        .setSwipeInMsgLayoutId(R.layout.swipe_yes)
                        .setSwipeOutMsgLayoutId(R.layout.swipe_no)
                        .setSwipeRotationAngle(0)
                        .setSwipeDistToDisplayMsg(100)
                        .setSwipeAnimTime(200));
    }
}