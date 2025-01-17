<script setup lang="ts">
import { ref, computed, inject } from "vue";
import axios from "axios";
import { useStateStore } from "@/stores/StateStore";
import { useSettingsStore } from "@/stores/settings/GeneralSettingsStore";

const address = inject<string>("backendHost");

const showImportDialog = ref(false);
const importRKNNFile = ref<File | null>(null);
const importLabelsFile = ref<File | null>(null);

// TODO gray out the button when model is uploading
const handleImport = async () => {
  if (importRKNNFile.value === null || importLabelsFile.value === null) return;

  const formData = new FormData();
  formData.append("rknn", importRKNNFile.value);
  formData.append("labels", importLabelsFile.value);

  useStateStore().showSnackbarMessage({
    message: "Importing Object Detection Model...",
    color: "secondary",
    timeout: -1
  });

  axios
    .post("/utils/importObjectDetectionModel", formData, {
      headers: { "Content-Type": "multipart/form-data" }
    })
    .then((response) => {
      useStateStore().showSnackbarMessage({
        message: response.data.text || response.data,
        color: "success"
      });
    })
    .catch((error) => {
      if (error.response) {
        useStateStore().showSnackbarMessage({
          color: "error",
          message: error.response.data.text || error.response.data
        });
      } else if (error.request) {
        useStateStore().showSnackbarMessage({
          color: "error",
          message: "Error while trying to process the request! The backend didn't respond."
        });
      } else {
        useStateStore().showSnackbarMessage({
          color: "error",
          message: "An error occurred while trying to process the request."
        });
      }
    });

  showImportDialog.value = false;

  importRKNNFile.value = null;
  importLabelsFile.value = null;
};

// Filters out models that are not supported by the current backend, and returns a flattened list.
const supportedModels = computed(() => {
  const { availableModels, supportedBackends } = useSettingsStore().general;
  return supportedBackends.flatMap((backend) => availableModels[backend] || []);
});

const exportModels = ref();
const openExportModelsPrompt = () => {
  exportModels.value.click();
};

const showModelsImportDialog = ref(false);
const importFile = ref<File | null>(null);
const handleObjectDetectionModelsImport = () => {
  if (importFile.value === null) return;

  const formData = new FormData();
  formData.append("data", importFile.value);

  axios
    .post(`/utils/objectDetectionModelsImport`, formData, {
      headers: { "Content-Type": "multipart/form-data" }
    })
    .then((response) => {
      useStateStore().showSnackbarMessage({
        message: response.data.text || response.data,
        color: "success"
      });
    })
    .catch((error) => {
      if (error.response) {
        useStateStore().showSnackbarMessage({
          color: "error",
          message: error.response.data.text || error.response.data
        });
      } else if (error.request) {
        useStateStore().showSnackbarMessage({
          color: "error",
          message: "Error while trying to process the request! The backend didn't respond."
        });
      } else {
        useStateStore().showSnackbarMessage({
          color: "error",
          message: "An error occurred while trying to process the request."
        });
      }
    });

  showImportDialog.value = false;
  importFile.value = null;
};
</script>

<template>
  <v-card dark class="mb-3" style="background-color: #006492">
    <v-card-title class="pa-6">Object Detection</v-card-title>
    <div class="pa-6 pt-0">
      <v-row>
        <v-col cols="12 ">
          <v-btn color="secondary" class="justify-center" @click="() => (showImportDialog = true)">
            <v-icon left class="open-icon"> mdi-import </v-icon>
            <span class="open-label">Import New Model</span>
          </v-btn>
          <v-dialog
            v-model="showImportDialog"
            width="600"
            @input="
              () => {
                importRKNNFile = null;
                importLabelsFile = null;
              }
            "
          >
            <v-card color="primary" dark>
              <v-card-title>Import New Object Detection Model</v-card-title>
              <v-card-text>
                Upload a new object detection model to this device that can be used in a pipeline. Naming convention
                should be <code>name-verticalResolution-horizontalResolution-modelType</code>. Additionally, the labels
                file ought to have the same name as the RKNN file, with <code>-labels</code> appended to the end. For
                example, if the RKNN file is named <code>note-640-640-yolov5s.rknn</code>, the labels file should be
                named <code>note-640-640-yolov5s-labels.txt</code>. Note that ONLY 640x640 YOLOv5 & YOLOv8 models
                trained and converted to `.rknn` format for RK3588 CPUs are currently supported!
                <v-row class="mt-6 ml-4 mr-8">
                  <v-file-input v-model="importRKNNFile" label="RKNN File" accept=".rknn" />
                </v-row>
                <v-row class="mt-6 ml-4 mr-8">
                  <v-file-input v-model="importLabelsFile" label="Labels File" accept=".txt" />
                </v-row>
                <v-row
                  class="mt-12 ml-8 mr-8 mb-1"
                  style="display: flex; align-items: center; justify-content: center"
                  align="center"
                >
                  <v-btn
                    color="secondary"
                    :disabled="importRKNNFile === null || importLabelsFile === null"
                    @click="handleImport"
                  >
                    <v-icon left class="open-icon"> mdi-import </v-icon>
                    <span class="open-label">Import Object Detection Model</span>
                  </v-btn>
                </v-row>
              </v-card-text>
            </v-card>
          </v-dialog>
        </v-col>
      </v-row>
      <v-row>
        <v-col cols="12" sm="6">
          <v-btn color="secondary" @click="openExportModelsPrompt">
            <v-icon left class="open-icon"> mdi-export </v-icon>
            <span class="open-label">Export Object Detection Models To Zip</span>
          </v-btn>
          <a
            ref="exportModels"
            style="color: black; text-decoration: none; display: none"
            :href="`http://${address}/api/settings/photonvision_models.zip`"
            download="photonvision-models.zip"
            target="_blank"
          />
        </v-col>
        <v-col cols="12" sm="6">
          <v-btn color="secondary" @click="() => (showModelsImportDialog = true)">
            <v-icon left class="open-icon"> mdi-import </v-icon>
            <span class="open-label">Import Object Detection Models From Zip</span>
          </v-btn>
          <v-dialog
            v-model="showModelsImportDialog"
            width="600"
            @input="
              () => {
                importFile = null;
              }
            "
          >
            <v-card color="primary" dark>
              <v-card-title>Import Settings</v-card-title>
              <v-card-text>
                Upload previously exported object detection models to this device. The file should be a `.zip` file.
                <v-row class="mt-6 ml-4 mr-8">
                  <v-file-input v-model="importFile" :accept="'.zip'" />
                </v-row>
                <v-row
                  class="mt-12 ml-8 mr-8 mb-1"
                  style="display: flex; align-items: center; justify-content: center"
                  align="center"
                >
                  <v-btn color="secondary" :disabled="importFile === null" @click="handleObjectDetectionModelsImport">
                    <v-icon left class="open-icon"> mdi-import </v-icon>
                    <span class="open-label">Import Object Detection Models</span>
                  </v-btn>
                </v-row>
              </v-card-text>
            </v-card>
          </v-dialog>
        </v-col>
      </v-row>
      <v-row>
        <v-col cols="12">
          <v-simple-table fixed-header height="100%" dense dark>
            <thead style="font-size: 1.25rem">
              <tr>
                <th class="text-left">Available Models</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="model in supportedModels" :key="model">
                <td>{{ model }}</td>
              </tr>
            </tbody>
          </v-simple-table>
        </v-col>
      </v-row>
    </div>
  </v-card>
</template>

<style scoped lang="scss">
.v-btn {
  width: 100%;
}
@media only screen and (max-width: 351px) {
  .open-icon {
    margin: 0 !important;
  }
  .open-label {
    display: none;
  }
}
.v-data-table {
  width: 100%;
  height: 100%;
  text-align: center;
  background-color: #006492 !important;

  th,
  td {
    background-color: #006492 !important;
    font-size: 1rem !important;
    color: white !important;
  }

  td {
    font-family: monospace !important;
  }

  tbody :hover td {
    background-color: #005281 !important;
  }

  ::-webkit-scrollbar {
    width: 0;
    height: 0.55em;
    border-radius: 5px;
  }

  ::-webkit-scrollbar-track {
    -webkit-box-shadow: inset 0 0 6px rgba(0, 0, 0, 0.3);
    border-radius: 10px;
  }

  ::-webkit-scrollbar-thumb {
    background-color: #ffd843;
    border-radius: 10px;
  }
}
</style>
