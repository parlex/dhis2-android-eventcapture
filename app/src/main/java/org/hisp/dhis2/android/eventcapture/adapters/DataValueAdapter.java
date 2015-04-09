/*
 * Copyright (c) 2015, dhis2
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis2.android.eventcapture.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.hisp.dhis2.android.eventcapture.adapters.rows.dataentry.DataEntryRow;
import org.hisp.dhis2.android.eventcapture.adapters.rows.dataentry.DataEntryRowTypes;
import org.hisp.dhis2.android.eventcapture.views.PinnedSectionListView.PinnedSectionListAdapter;

import java.util.List;

import static org.hisp.dhis2.android.sdk.utils.Preconditions.isNull;

public final class DataValueAdapter extends BaseAdapter implements PinnedSectionListAdapter {
    private List<DataEntryRow> mRows;
    private LayoutInflater mInflater;

    public DataValueAdapter(LayoutInflater inflater) {
        mInflater = isNull(inflater, "LayoutInflater object must not be null");
    }

    @Override
    public int getCount() {
        if (mRows != null) {
            return mRows.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        if (mRows != null) {
            return mRows.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (mRows != null) {
            return mRows.get(position).getView(mInflater, convertView, parent);
        } else {
            return null;
        }
    }

    @Override
    public int getViewTypeCount() {
        return DataEntryRowTypes.values().length;
    }

    @Override
    public int getItemViewType(int position) {
        if (mRows != null) {
            return mRows.get(position).getViewType();
        } else {
            return 0;
        }
    }

    @Override
    public boolean isItemViewTypePinned(int viewType) {
        return DataEntryRowTypes.PROGRAM_STAGE_SECTION.ordinal() == viewType;
    }

    public void swap(List<DataEntryRow> rows) {
        boolean notifyChanged = mRows != rows;
        mRows = rows;

        if (notifyChanged) {
            notifyDataSetChanged();
        }
    }
}
