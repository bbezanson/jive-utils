package com.jivesoftware.os.jive.utils.hwal.shared.hbase;

import com.google.common.base.Charsets;
import com.jivesoftware.jive.platform.miru.miru.api.activity.MiruPartitionId;
import com.jivesoftware.jive.platform.miru.miru.api.activity.MiruPartitionedActivity;
import com.jivesoftware.jive.platform.miru.miru.api.base.MiruTenantId;
import com.jivesoftware.jive.platform.miru.miru.wal.activity.hbase.MiruActivitySipWALColumnKey;
import com.jivesoftware.jive.platform.miru.miru.wal.activity.hbase.MiruActivityWALColumnKey;
import com.jivesoftware.jive.platform.miru.miru.wal.activity.hbase.MiruActivityWALRow;
import com.jivesoftware.os.jive.utils.base.interfaces.CallbackStream;
import com.jivesoftware.os.jive.utils.id.TenantId;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.ColumnValueAndTimestamp;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.RowColumnValueStore;

/** @author jonathan */
public class WALReaderImpl implements WALReader {

    private final RowColumnValueStore<TenantId, MiruActivityWALRow, MiruActivityWALColumnKey, MiruPartitionedActivity, ? extends Exception>
        activityWAL;
    private final RowColumnValueStore<TenantId, MiruActivityWALRow, MiruActivitySipWALColumnKey, MiruPartitionedActivity, ? extends Exception>
        activitySipWAL;

    public WALReaderImpl(
        RowColumnValueStore<TenantId, MiruActivityWALRow, MiruActivityWALColumnKey, MiruPartitionedActivity, ? extends Exception> activityWAL,
        RowColumnValueStore<TenantId, MiruActivityWALRow, MiruActivitySipWALColumnKey, MiruPartitionedActivity, ? extends Exception> activitySipWAL) {

        this.activityWAL = activityWAL;
        this.activitySipWAL = activitySipWAL;
    }

    private MiruActivityWALRow rowKey(MiruPartitionId partition) {
        return new MiruActivityWALRow(partition.getId());
    }

    @Override
    public void stream(TenantId tenantId, TopicId topicId, long afterTimestamp, StreamWAL streamWAL)
        throws Exception {
        MiruActivityWALRow rowKey = rowKey(topicId);

        streamFromActivityWAL(tenantId, rowKey, afterTimestamp, streamWAL);
    }

    @Override
    public void streamSip(TenantId tenantId, TopicId topicId, long afterTimestamp, StreamWAL streamWAL)
        throws Exception {
        MiruActivityWALRow rowKey = rowKey(topicId);

        streamFromActivitySipWAL(tenantId, rowKey, afterTimestamp, streamWAL);
    }

    private void streamFromActivityWAL(TenantId tenantId, MiruActivityWALRow rowKey, long afterTimestamp,
        final StreamWAL streamMiruActivityWAL) throws Exception {

        MiruActivityWALColumnKey start = new MiruActivityWALColumnKey(MiruPartitionedActivity.Type.ACTIVITY.getSort(), afterTimestamp);

        activityWAL.getEntrys(tenantId, rowKey, start, Long.MAX_VALUE, 1000, false, null, null,
            new CallbackStream<ColumnValueAndTimestamp<MiruActivityWALColumnKey, MiruPartitionedActivity, Long>>() {
                @Override
                public ColumnValueAndTimestamp<MiruActivityWALColumnKey, MiruPartitionedActivity, Long> callback(
                    ColumnValueAndTimestamp<MiruActivityWALColumnKey, MiruPartitionedActivity, Long> v) throws Exception {

                    if (v != null) {
                        if (!streamMiruActivityWAL.stream(v.getColumn().getCollisionId(), v.getValue(), v.getTimestamp())) {
                            return null;
                        }
                    }
                    return v;
                }
            });
    }

    private void streamFromActivitySipWAL(TenantId tenantId, MiruActivityWALRow rowKey, long afterTimestamp,
        final StreamWAL streamMiruActivityWAL) throws Exception {

        MiruActivitySipWALColumnKey start = new MiruActivitySipWALColumnKey(MiruPartitionedActivity.Type.ACTIVITY.getSort(), afterTimestamp);

        activitySipWAL.getEntrys(tenantId, rowKey, start, Long.MAX_VALUE, 1000, false, null, null,
            new CallbackStream<ColumnValueAndTimestamp<MiruActivitySipWALColumnKey, MiruPartitionedActivity, Long>>() {
                @Override
                public ColumnValueAndTimestamp<MiruActivitySipWALColumnKey, MiruPartitionedActivity, Long> callback(
                    ColumnValueAndTimestamp<MiruActivitySipWALColumnKey, MiruPartitionedActivity, Long> v) throws Exception {

                    if (v != null) {
                        if (!streamMiruActivityWAL.stream(v.getColumn().getCollisionId(), v.getValue(), v.getTimestamp())) {
                            return null;
                        }
                    }
                    return v;
                }
            });
    }

    @Override
    public MiruPartitionedActivity findExisting(MiruTenantId tenantId, MiruPartitionId partitionId, MiruPartitionedActivity activity) throws Exception {
        return activityWAL.get(
            new TenantId(new String(tenantId.getBytes(), Charsets.UTF_8)),
            new MiruActivityWALRow(partitionId.getId()),
            new MiruActivityWALColumnKey(MiruPartitionedActivity.Type.ACTIVITY.getSort(), activity.timestamp),
            null, null);
    }

}
