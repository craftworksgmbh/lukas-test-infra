
from kedro.io import PartitionedDataSet
from typing import Dict, Any
from copy import deepcopy
from tqdm import tqdm


class PartitionedDataSetPb(PartitionedDataSet):
    '''
        The only difference to a Partitioned Dataset is that
        during the saving step a progress bar is shown, using tqdm
    '''

    def _save(self, data: Dict[str, Any]) -> None:
        if self._overwrite and self._filesystem.exists(self._normalized_path):
            self._filesystem.rm(self._normalized_path, recursive=True)

        for partition_id, partition_data in tqdm(sorted(data.items())):
            kwargs = deepcopy(self._dataset_config)
            partition = self._partition_to_path(partition_id)
            # join the protocol back since tools like PySpark may rely on it
            kwargs[self._filepath_arg] = self._join_protocol(partition)
            dataset = self._dataset_type(**kwargs)  # type: ignore
            if callable(partition_data):
                partition_data = partition_data()
            dataset.save(partition_data)
        self._invalidate_caches()