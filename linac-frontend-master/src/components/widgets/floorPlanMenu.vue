<template>
  <div>
    <v-card class="m-3">
      <v-list dense>
        <v-list-item>
          <v-list-item-content>
            <v-text-field label="Filter" hint="Case-sensitive" v-model="filterText"></v-text-field>
          </v-list-item-content>
        </v-list-item>
          <v-list-item v-for="item in items" :key="item.title" @click="executeEvent(item.eventName)">
            <v-list-item-icon>
              <font-awesome-icon :icon=item.icon />
            </v-list-item-icon>

            <v-list-item-content>
              <v-list-item-title>{{ item.title }}</v-list-item-title>
            </v-list-item-content>
          </v-list-item>

          <v-list-item v-for="item in itemsInline" :key="item.title" class="float-left"
            @click="executeEvent(item.eventName)">
            <v-list-item-content>
              <font-awesome-icon :icon=item.icon />
            </v-list-item-content>
          </v-list-item>

          <v-list-item @click="{}" v-b-modal.modal-upload style="clear: both">
            <v-list-item-icon>
              <font-awesome-icon icon="upload" />
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title>
                Upload / Download </v-list-item-title>
            </v-list-item-content>
          </v-list-item>
        </v-list>
          <v-divider />
          <v-list-item>
            <v-list-item-content>
              <v-list-item-subtitle class="text-wrap">{{
            tooltip
          }}</v-list-item-subtitle>
            </v-list-item-content>
          </v-list-item>
  </v-card>

    <b-modal v-model="show" id="modal-upload" title="Upload or download the floor plan">
      <UploadDownload @closeCard="closeCardRefresh" />
      <template #modal-footer>
        <div>
          <b-button
            variant="primary"
            class="float-right"
            @click="show=false"
          >
            Close
          </b-button>
        </div>
      </template>
    </b-modal>

  </div>
</template>

<script>
  import UploadDownload from "@/components/widgets/uploadDownload.vue";
  // import About from '../views/About.vue';
  export default {
    name: "FloorPlanMenu",
    components: {
      UploadDownload
    },
    data() {
      return {
        items: [{
            title: "Add Sensor",
            eventName: "gridAddSensor",
            icon: "lightbulb"
          },
          {
            title: "Add Entity",
            eventName: "gridAddEntity",
            icon: "layer-group"
          },
          {
            title: "Add Agent",
            eventName: "gridAddAgent",
            icon: "walking"
          },
        ],
        itemsInline: [{
            eventName: "gridPanUp",
            icon: "angle-up"
          },
          {
            eventName: "gridPanDown",
            icon: "angle-down"
          },
          {
            eventName: "gridPanLeft",
            icon: "angle-left"
          },
          {
            eventName: "gridPanRight",
            icon: "angle-right"
          },
          {
            eventName: "gridClear",
            icon: "trash"
          },
        ],
        y: window.innerHeight - 57,
        x: window.innerWidth * 0.15,
        filterText: this.$store.state.filterText,
        showUploadDownload: false,
        tooltip: "x-y",
        show: false
      };
    },
    methods: {
      executeEvent(eventName) {
        this.$root.$emit(eventName);
      },
      closeCardRefresh() {
        this.showUploadDownload = false;
      },
      onResize() {
        this.y = window.innerHeight - 57;
        this.x = window.innerWidth * 0.15;
      },
    },
    watch: {
      filterText: function (newText) {
        this.$store.commit("updateFilterText", newText);
      },
    },
    mounted() {
      this.$root.$on("tooltip", (data) => {
        this.tooltip = data;
      });
    },
  };
</script>
