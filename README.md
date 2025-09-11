<h1 align="center" id="title">Multi-Trail-Data-Recorder</h1>

<p align="center"><img src="https://socialify.git.ci/Sarath-SD10/Multi-Trail-Data-Recorder/image?custom_language=Java&amp;description=1&amp;font=Inter&amp;forks=1&amp;issues=1&amp;language=1&amp;name=1&amp;owner=1&amp;pattern=Charlie+Brown&amp;pulls=1&amp;stargazers=1&amp;theme=Auto" alt="project-image"></p>

[![Android CI](https://github.com/S-Sarath10/Multi-Trail-Data-Recorder/actions/workflows/android.yml/badge.svg)](https://github.com/S-Sarath10/Multi-Trail-Data-Recorder/actions/workflows/android.yml)

----
# Multi-Trail-Data-Recorder

A simple web-based application for recording agricultural field trial data points, designed for easy use on Android devices and web browsers.

## About

Multi-Trail-Data-Recorder provides an intuitive interface for researchers and field workers to record, manage, and analyze data from multiple agricultural trials directly from the field. This application simplifies data collection, ensuring accurate and organized capture of trial information.

## Features

  * Web-based interface accessible via modern browsers and also with the android application.
  * Android compatibility for in-field data entry.
  * Simple and user-friendly design for easy data recording.
  * Supports recording multiple trial data points for agricultural research.
  * You can take a picture of a particular entry, and the image will be automatically saved with the entry's name as the file name.
  * You can mark entries so you can easily refer to them or visit them again later.
  * Lightweight and efficient for field use.
  * Open-source under the GPL-3.0 License.

## Input file

You can either import the file (.csv) or create your own trail file with the inbuilt manager

To prepare the input file for the Multi-Trail-Data-Recorder, you'll need to create a **CSV (Comma-Separated Values)** file with specific headers.

### **CSV File Structure**

The file must be a plain text file where each value is separated by a comma.

* **First Row:** This row must contain the required **headers** exactly as specified: `TRIAL_ID`, `YEAR`, `SEASON`, `TRIAL_NAME`, `CHARACTERS`, and `GENOTYPES`.
* **Subsequent Rows:** Each row after the header represents a specific agricultural trial or a subset of data points.

***

Based on the updated information, here are the corrected and refined details for preparing the input file for the Multi-Trail-Data-Recorder.

***

### **Required Columns and Their Content**

1.  **`TRIAL_ID`**: A unique identifier for the trial. It is preferred to use an **acronym**.
2.  **`YEAR`**: The year of the trial, preferably in a **two-digit format** (e.g., `25` for 2025).
3.  **`SEASON`**: The specific season of the trial, with preferred formats being `RY` or `SU`.
4.  **`TRIAL_NAME`**: A name or code to identify a subset within the trial.
5.  **`CHARACTERS`**: This crucial column defines the variables being measured. An **acronym is preferred** to keep the interface less cluttered. It uses a specific syntax to indicate the variable name and its data type, which in turn determines the keyboard layout on an Android device.
6.  **`GENOTYPES`**: A **comma-separated list** of plot numbers or genotype identifiers (e.g., `101,102,103,104,...`). These identifiers must match the data points you intend to record.

-----

### How to Create the `CHARACTERS` Column

The `CHARACTERS` column is crucial for defining the variables you're measuring. It uses a specific syntax to keep the interface simple and to control the keyboard type for data entry on Android devices.

Each variable definition should follow this format:

```
<ACRONYM>:<DATA_TYPE>
```

  * **`<ACRONYM>`**: A short code for the variable (e.g., `PH` for Plant Height).
  * **`<DATA_TYPE>`**: Specifies the input type and keyboard:
      * **`num`**: for numeric input (displays a numeric keyboard).
      * **`text`**: for text input (displays an alphabetical keyboard).

-----

#### **Examples**

You can list multiple variables in a single row, separated by commas.

| Trait | Acronym | Data Type | Example |
| :--- | :--- | :--- | :--- |
| Plant Height | `PH` | `num` | `PH:num` |
| Plant Type | `PT` | `text` | `PT:text` |
| Disease Score | `DIS` | `num` | `DIS:num` |

**Combined Example:**

To define all three variables for a single trial, your `CHARACTERS` column entry would look like this:

```
PH:num,PT:text,DIS:num
```

This single string tells the app to display a numeric keyboard for "Plant Height," a text keyboard for "Plant Type," and another numeric one for "Disease Score."


***

By following these specific formatting rules for each column, the application can accurately interpret your input file and present the correct data entry interface to the user.

***

### Trait Limit

By default, the application allows you to select a maximum of **6 traits** at a time. This limit is set to ensure a smooth and manageable user experience on smaller screens like those on Android mobile phones.

You can change this limit to any number you prefer. However, be aware that choosing too many traits may make the interface cluttered and difficult to use on `mobile devices`. For this reason, it is recommended to use a `tablet` if you need to work with a large number of traits simultaneously.

***

## Installation

Clone this repository:

```bash
git clone https://github.com/S-Sarath10/Multi-Trail-Data-Recorder.git
```

Open the `index.html` file in a web browser or deploy on an Android device supporting web applications.
Start recording your agricultural trial data.

## Usage

  * Use the web interface or Android platform to enter trial data.
  * Follow on-screen instructions to add and manage data points.
  * Data can be saved and reviewed as needed.

## License

This project is licensed under the [GPL-3.0 License](https://www.gnu.org/licenses/gpl-3.0.html).
