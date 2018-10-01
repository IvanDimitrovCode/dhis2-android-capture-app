package org.dhis2.usescases.datasets.datasetInitial;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.PopupMenu;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.databinding.ActivityDatasetInitialBinding;
import org.dhis2.databinding.ItemCategoryComboBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.utils.Constants;
import org.dhis2.utils.CustomViews.OrgUnitDialog;
import org.dhis2.utils.CustomViews.PeriodDialog;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.category.CategoryModel;
import org.hisp.dhis.android.core.category.CategoryOptionModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.period.PeriodType;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

public class DataSetInitialActivity extends ActivityGlobalAbstract implements DataSetInitialContract.View {

    private ActivityDatasetInitialBinding binding;
    View selectedView;
    @Inject
    DataSetInitialContract.Presenter presenter;

    private HashMap<String, CategoryOptionModel> selectedCatOptions;
    private OrganisationUnitModel selectedOrgUnit;
    private Date selectedPeriod;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String dataSetUid = getIntent().getStringExtra(Constants.DATA_SET_UID);
        ((App) getApplicationContext()).userComponent().plus(new DataSetInitialModule(dataSetUid)).inject(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_dataset_initial);
        binding.setPresenter(presenter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init(this);
    }

    @Override
    protected void onPause() {
        presenter.onDettach();
        super.onPause();
    }

    @Override
    public void setAccessDataWrite(Boolean canWrite) {

    }

    @Override
    public void setData(DataSetInitialModel dataSetInitialModel) {
        binding.setDataSetModel(dataSetInitialModel);
        binding.catComboContainer.removeAllViews();
        selectedCatOptions = new HashMap<>();
        for (CategoryModel categoryModel : dataSetInitialModel.categories()) {
            selectedCatOptions.put(categoryModel.uid(), null);
            ItemCategoryComboBinding categoryComboBinding = ItemCategoryComboBinding.inflate(getLayoutInflater(), binding.catComboContainer, false);
            categoryComboBinding.inputLayout.setHint(categoryModel.displayName());
            categoryComboBinding.inputEditText.setOnClickListener(view -> {
                selectedView = view;
                presenter.onCatOptionClick(categoryModel.uid());
            });
            binding.catComboContainer.addView(categoryComboBinding.getRoot());
        }
        checkActionVisivbility();
    }

    /**
     * When changing orgUnit, date must be cleared
     */
    @Override
    public void showOrgUnitDialog(List<OrganisationUnitModel> data) {
        OrgUnitDialog orgUnitDialog = OrgUnitDialog.newInstace(false);
        orgUnitDialog.setOrgUnits(data);
        orgUnitDialog.setTitle(getString(R.string.org_unit))
                .setPossitiveListener(v -> {
                    if (orgUnitDialog.getSelectedOrgUnit() != null) {
                        selectedOrgUnit = orgUnitDialog.getSelectedOrgUnitModel();
                        binding.dataSetOrgUnitEditText.setText(selectedOrgUnit.displayName());
                        binding.dataSetPeriodEditText.setText("");
                    }
                    checkActionVisivbility();
                    orgUnitDialog.dismiss();
                })
                .setNegativeListener(v -> orgUnitDialog.dismiss());
        orgUnitDialog.show(getSupportFragmentManager(), OrgUnitDialog.class.getSimpleName());
    }

    @Override
    public void showPeriodSelector(PeriodType periodType) {
        new PeriodDialog()
                .setPeriod(periodType)
//                .setMinDate() TODO: Depends on dataSet expiration settings and orgUnit Opening date
//                .setMaxDate() TODO: Depends on dataSet open Future settings. Default: TODAY
                .setMaxDate(DateUtils.getInstance().getCalendar().getTime())
                .setPossitiveListener(selectedDate -> {
                    this.selectedPeriod = selectedDate;
                    binding.dataSetPeriodEditText.setText(DateUtils.getInstance().getPeriodUIString(periodType, selectedDate));
                    checkActionVisivbility();
                })
                .show(getSupportFragmentManager(), PeriodDialog.class.getSimpleName());
    }

    @Override
    public void showCatComboSelector(String catOptionUid, List<CategoryOptionModel> data) {
        PopupMenu menu = new PopupMenu(this, selectedView, Gravity.BOTTOM);
//        menu.getMenu().add(Menu.NONE, Menu.NONE, 0, viewModel.label()); Don't show label
        for (CategoryOptionModel optionModel : data)
            menu.getMenu().add(Menu.NONE, Menu.NONE, data.indexOf(optionModel), optionModel.displayName());

        menu.setOnDismissListener(menu1 -> selectedView = null);
        menu.setOnMenuItemClickListener(item -> {
            if (selectedCatOptions == null)
                selectedCatOptions = new HashMap<>();
            selectedCatOptions.put(catOptionUid, data.get(item.getOrder()));
            ((TextInputEditText) selectedView).setText(data.get(item.getOrder()).displayName());
            checkActionVisivbility();
            return false;
        });
        menu.show();
    }

    private void checkActionVisivbility() {
        boolean visible = true;
        if (selectedOrgUnit == null)
            visible = false;
        if (selectedPeriod == null)
            visible = false;
        for (String key : selectedCatOptions.keySet()) {
            if (selectedCatOptions.get(key) == null)
                visible = false;
        }

        binding.actionButton.setVisibility(visible ? View.VISIBLE : View.GONE);

    }
}