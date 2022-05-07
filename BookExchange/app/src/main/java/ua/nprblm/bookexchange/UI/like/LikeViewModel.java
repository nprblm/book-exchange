package ua.nprblm.bookexchange.UI.like;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LikeViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public LikeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is like fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}