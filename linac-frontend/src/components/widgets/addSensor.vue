<template>
  <div>
    <v-card class="mx-auto" max-width="600" outlined elevation="2" shaped>
      <v-card-title>Please enter further details about the sensor
        <v-btn text @click="close"> &times; </v-btn>
      </v-card-title>
      <v-card-text>
        <v-form v-model="isValid">
          <v-text-field
            label="Name"
            v-model="name"
            required
            :rules="nameRules"
          ></v-text-field>
          <label>Type: </label>
          <select
            v-model="type"
            class="form-control"
            required
            :rules="typeRules"
          >
            <option
              v-for="option in typeOptions"
              v-bind:key="option.value"
              :value="option.value"
            >
              {{ option.text }}
            </option>
          </select>
          <v-checkbox v-model="walkable" label="walkable"></v-checkbox>
          <v-text-field
            label="Trigger Frequency (s)"
            v-model="triggerFrequency"
            required
            hint="e.g: 5"
            :rules="triggerFrequencyRules"
          ></v-text-field>
        </v-form>
      </v-card-text>
      <v-card-actions>
        <v-btn outlined rounded text :disabled="!isValid" @click="submit">
          Submit
        </v-btn>
      </v-card-actions>
    </v-card>
  </div>
</template>

<script>
import position from "../../models/position";
import sensor from "../../models/sensor";
export default {
  name: "AddSensor",
  props: ["physicalArea", "interactArea"],
  data() {
    return {
      name: "",
      triggerFrequency: "0",
      walkable: false,
      typeOptions: [],
      type: "",
      isValid: true,
      nameRules: [(v) => !!v || "Required", (v)=> !this.$store.getters.sensorNames.includes(v)||"Enter a unique name"],
      typeRules: [(v) => !!v || "Required"],
      triggerFrequencyRules: [
        (v) => /^\d*\.?\d+$/.test(v) ||
          "Enter a number or a decimal followed by a number. You need to add a 0 for active sensors.",
      ],
    };
  },
  methods: {
    submit() {
      let sensorObject = new sensor(
        this.name,
        this.getListPositions(this.physicalArea),
        this.getListPositions(this.interactArea),
        this.triggerFrequency == ""
          ? 0
          : Number(this.triggerFrequency) * 1000000000,
        this.typeOptions.find((option) => option.value == this.type).send,
        this.walkable,
        this.isPassiveType() ? "passive" : "active"
      );
      this.$store.commit("addSensor", sensorObject);
      this.$emit("closeSensorForm");
    },
    close(){
      this.$emit("closeForm");
    },
    getListPositions(positions) {
      let positionObjects = [];
      positions.forEach((id) => {
        let coords = id.split("-");
        positionObjects.push(
          new position(parseInt(coords[0]), parseInt(coords[1]))
        );
      });
      return positionObjects;
    },
    isPassiveType() {
      if (
        this.$store.state.passiveSensors.includes(
          this.typeOptions.find((option) => option.value == this.type).send
        )
      ) {
        return true;
      }
      return false;
    },
    getTypeOptions() {
      let passiveSensors = this.$store.state.passiveSensors;
      let j = passiveSensors.length;
      for (let i = 0; i < passiveSensors.length; i++) {
        let el = passiveSensors[i].split(".");
        this.typeOptions.push({
          value: i,
          text: el[2],
          send: passiveSensors[i],
        });
      }

      if (this.interactArea.size > 0) {
        let activeSensors = this.$store.state.activeSensors;
        for (let i = 0; i < activeSensors.length; i++) {
          let el = activeSensors[i].split(".");
          this.typeOptions.push({
            value: i + j,
            text: el[2],
            send: activeSensors[i],
          });
        }
      }
    },
  },
  beforeMount() {
    this.getTypeOptions();
    this.type = this.typeOptions[0].value;
  },
};
</script>

