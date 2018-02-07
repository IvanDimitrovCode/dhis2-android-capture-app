package com.dhis2.usescases.programDetailTablet;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;

import com.android.databinding.library.baseAdapters.BR;
import com.dhis2.usescases.main.program.HomeViewModel;

import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.List;

/**
 * Created by frodriguez on 10/31/2017.
 */

public class ProgramDetailViewHolder extends RecyclerView.ViewHolder {

    ViewDataBinding binding;

    public ProgramDetailViewHolder(ViewDataBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(ProgramDetailContractModule.Presenter presenter,
                     ProgramModel program,
                     String orgUnit,
                     List<String> attributes,
                     String stage) {
        binding.setVariable(BR.presenter, presenter);
        binding.setVariable(BR.program, program);
        binding.setVariable(BR.orgUnit, orgUnit);
        binding.setVariable(BR.attribute, attributes);
        binding.setVariable(BR.stage, stage);
        binding.executePendingBindings();
    }


}
