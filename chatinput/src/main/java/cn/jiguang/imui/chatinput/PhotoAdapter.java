package cn.jiguang.imui.chatinput;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

  private Context mContext;

  private List<FileItem> mPhotos;
  private List<String> mSendFiles;

  private SparseBooleanArray mSelectedItems;

  private OnFileSelectedListener mListener;

  private int mPhotoSide;    // 图片边长

  public PhotoAdapter(List<FileItem> list, int height) {
    mSelectedItems = new SparseBooleanArray();
    if (list == null) {
      mPhotos = new ArrayList<>();
    } else {
      mPhotos = list;
    }
    mPhotoSide = height;
  }

  public List<String> getSelectedFiles() {
    return mSendFiles;
  }

  public void setOnPhotoSelectedListener(OnFileSelectedListener listener) {
    mListener = listener;
  }

  public void setSelectedFiles(List<String> list) {
    mSendFiles = list;
  }

  public void resetCheckedState() {
    mSendFiles.clear();
    for (int i = 0; i < mSelectedItems.size(); i++) {
      // 处于选中状态
      if (mSelectedItems.get(i)) {
        mSelectedItems.delete(i);
        mSelectedItems.put(i, false);
        notifyDataSetChanged();
      }
    }
    mSelectedItems.clear();
  }

  @Override public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    mContext = parent.getContext();

    RelativeLayout photoSelectLayout = (RelativeLayout) LayoutInflater.from(mContext)
        .inflate(R.layout.item_photo_select, parent, false);
    PhotoViewHolder holder = new PhotoViewHolder(photoSelectLayout);
    return holder;
  }

  @Override public void onBindViewHolder(final PhotoViewHolder holder, int position) {
    if (holder.container.getMeasuredWidth() != mPhotoSide) {
      FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(mPhotoSide, mPhotoSide);
      layoutParams.rightMargin = DisplayUtil.dp2px(mContext, 8);
      holder.container.setLayoutParams(layoutParams);
    }

    final FileItem item = mPhotos.get(position);
    String path = item.getFilePath();
    File file = new File(path);
    Glide.with(mContext)
        .load(file)
        .placeholder(R.drawable.jmui_picture_not_found)
        .crossFade()
        .into(holder.ivPhoto);

    if (position < mSelectedItems.size() && mSelectedItems.valueAt(position)) {
      if (mSelectedItems.get(position)) {
        holder.ivTick.setVisibility(VISIBLE);
        holder.ivShadow.setVisibility(VISIBLE);
        addSelectedAnimation(holder.ivPhoto, holder.ivShadow);
      } else {
        holder.ivTick.setVisibility(GONE);
        holder.ivShadow.setVisibility(GONE);
        addDeselectedAnimation(holder.ivPhoto, holder.ivShadow);
      }
    }

    if (item.getType() == FileItem.Type.Video) {
      holder.tvDuration.setVisibility(View.VISIBLE);
      holder.tvDuration.setText(((VideoItem) item).getDuration());
      holder.setIsRecyclable(false);
    }

    holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        if (holder.ivTick.getVisibility() == GONE && !mSelectedItems.get(
            holder.getAdapterPosition())) {
          holder.setIsRecyclable(false);

          mSelectedItems.put(holder.getAdapterPosition(), true);
          mSendFiles.add(mPhotos.get(holder.getAdapterPosition()).getFilePath());

          holder.ivTick.setVisibility(VISIBLE);
          holder.ivShadow.setVisibility(VISIBLE);

          if (item.getType() == FileItem.Type.Video) {
            addSelectedAnimation(holder.ivPhoto, holder.ivShadow, holder.ivTick);
          } else {
            addSelectedAnimation(holder.ivPhoto, holder.ivShadow);
          }

          if (mListener != null) {
            mListener.onFileSelected();
          }
        } else {
          holder.setIsRecyclable(true);

          mSelectedItems.delete(holder.getAdapterPosition());
          mSendFiles.remove(mPhotos.get(holder.getAdapterPosition()).getFilePath());

          holder.ivTick.setVisibility(GONE);
          holder.ivShadow.setVisibility(GONE);

          if (item.getType() == FileItem.Type.Video) {
            addDeselectedAnimation(holder.ivPhoto, holder.ivShadow, holder.ivTick);
          } else {
            addDeselectedAnimation(holder.ivPhoto, holder.ivShadow);
          }

          if (mListener != null) {
            mListener.onFileDeselected();
          }
        }
      }
    });
  }

  @Override public int getItemCount() {
    return mPhotos.size();
  }

  private void addDeselectedAnimation(View... views) {
    List<Animator> valueAnimators = new ArrayList<>();
    for (View v : views) {
      ObjectAnimator scaleX = ObjectAnimator.ofFloat(v, "scaleX", 1.0f);
      ObjectAnimator scaleY = ObjectAnimator.ofFloat(v, "scaleY", 1.0f);

      valueAnimators.add(scaleX);
      valueAnimators.add(scaleY);
    }

    AnimatorSet set = new AnimatorSet();
    set.playTogether(valueAnimators);
    set.setDuration(150);
    set.start();
  }

  private void addSelectedAnimation(View... views) {
    List<Animator> valueAnimators = new ArrayList<>();
    for (View v : views) {
      ObjectAnimator scaleX = ObjectAnimator.ofFloat(v, "scaleX", 0.90f);
      ObjectAnimator scaleY = ObjectAnimator.ofFloat(v, "scaleY", 0.90f);

      valueAnimators.add(scaleX);
      valueAnimators.add(scaleY);
    }

    AnimatorSet set = new AnimatorSet();
    set.playTogether(valueAnimators);
    set.setDuration(150);
    set.start();
  }

  public interface OnFileSelectedListener {

    void onFileSelected();

    void onFileDeselected();
  }

  static final class PhotoViewHolder extends RecyclerView.ViewHolder {

    View container;
    TextView tvDuration;
    ImageView ivPhoto;
    ImageView ivShadow;
    ImageView ivTick;

    PhotoViewHolder(View itemView) {
      super(itemView);
      container = itemView;
      tvDuration = (TextView) itemView.findViewById(R.id.text_photoselect_duration);
      ivPhoto = (ImageView) itemView.findViewById(R.id.image_photoselect_photo);
      ivShadow = (ImageView) itemView.findViewById(R.id.image_photoselect_shadow);
      ivTick = (ImageView) itemView.findViewById(R.id.image_photoselect_tick);
    }
  }
}
