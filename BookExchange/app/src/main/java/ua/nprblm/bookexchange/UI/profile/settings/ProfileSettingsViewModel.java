package ua.nprblm.bookexchange.UI.profile.settings;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ProfileSettingsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ProfileSettingsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is profile fragment");
    }

}
