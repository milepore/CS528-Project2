package com.bignerdranch.android.criminalintent

import com.google.mlkit.vision.demo.GraphicOverlay
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.doOnLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bignerdranch.android.criminalintent.databinding.FragmentCrimeDetailBinding
import com.google.mlkit.vision.demo.VisionImageProcessor
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date
import com.google.mlkit.vision.demo.kotlin.facedetector.FaceDetectorProcessor
import com.google.mlkit.vision.demo.rotatedBitmap
import com.google.mlkit.vision.face.FaceDetectorOptions
import android.util.Log
import com.google.mlkit.vision.demo.CameraImageGraphic
import com.google.mlkit.vision.demo.kotlin.facemeshdetector.FaceMeshDetectorProcessor


private const val DATE_FORMAT = "EEE, MMM, dd"


class CrimeDetailFragment : Fragment() {

    private var _binding: FragmentCrimeDetailBinding? = null
    private lateinit var faceMeshProcessor: FaceMeshDetectorProcessor
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val args: CrimeDetailFragmentArgs by navArgs()

    private val crimeDetailViewModel: CrimeDetailViewModel by viewModels {
        CrimeDetailViewModelFactory(args.crimeId)
    }

    private val selectSuspect = registerForActivityResult(
        ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        uri?.let { parseContactSelection(it) }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        faceMeshProcessor = FaceMeshDetectorProcessor(requireContext())
    }
    private var photoName: String? = null
    private var nextPhotoIndex = 0
    private val maxPhotos = 4

    private val takePhoto = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { didTakePhoto: Boolean ->
        if (didTakePhoto && photoName != null) {
            crimeDetailViewModel.updateCrime { oldCrime ->
                val filteredPhotoFileNames = oldCrime.photoFileNames.filter { it.isNotBlank() }.toMutableList()

                while (filteredPhotoFileNames.size <= nextPhotoIndex) {
                    filteredPhotoFileNames.add("")
                }
                filteredPhotoFileNames[nextPhotoIndex] = photoName!!

                println("Updated photo file names: $filteredPhotoFileNames")

                nextPhotoIndex = (nextPhotoIndex + 1) % maxPhotos

                oldCrime.copy(photoFileNames = filteredPhotoFileNames)
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentCrimeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


//        binding.enableMeshDetection.setOnCheckedChangeListener { _, isChecked ->
//            Log.d("CheckboxChanged", "Mesh detection is: $isChecked")
//
//            // update faceMeshProcessor settings
//            faceMeshProcessor.setMeshDetectionEnabled(isChecked)
//
//            setupProcessor()
//            crimeDetailViewModel.crime.value?.let { updatePhotos(it) }
//        }

        binding.apply {
            crimeTitle.doOnTextChanged { text, _, _, _ ->
                crimeDetailViewModel.updateCrime { oldCrime ->
                    oldCrime.copy(title = text.toString())
                }
            }

            crimeSolved.setOnCheckedChangeListener { _, isChecked ->
                crimeDetailViewModel.updateCrime { oldCrime ->
                    oldCrime.copy(isSolved = isChecked)
                }
            }

            crimeSuspect.setOnClickListener {
                selectSuspect.launch(null)
            }

            val selectSuspectIntent = selectSuspect.contract.createIntent(
                requireContext(),
                null
            )
            crimeSuspect.isEnabled = canResolveIntent(selectSuspectIntent)

            crimeCamera.setOnClickListener {
                photoName = "IMG_${Date()}.JPG"
                val photoFile = File(
                    requireContext().applicationContext.filesDir,
                    photoName!!
                )
                val photoUri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.bignerdranch.android.criminalintent.fileprovider",
                    photoFile
                )

                takePhoto.launch(photoUri)
            }

            val captureImageIntent = takePhoto.contract.createIntent(
                requireContext(),
                null
            )
            crimeCamera.isEnabled = canResolveIntent(captureImageIntent)

            ChooseCheckBox(arrayOf(
                binding.enableFaceDetection,
                binding.enableContourDetection,
                binding.enableMeshDetection,
                binding.enableSelfieSegmentation
            )
            )
//            val nullTextView: TextView = null
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                crimeDetailViewModel.crime.collect { crime ->
                    crime?.let { updateUi(it) }
                }
            }
        }

        setFragmentResultListener(
            DatePickerFragment.REQUEST_KEY_DATE
        ) { _, bundle ->
            val newDate =
                bundle.getSerializable(DatePickerFragment.BUNDLE_KEY_DATE) as Date
            crimeDetailViewModel.updateCrime { it.copy(date = newDate) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    var imageProcessor: VisionImageProcessor? = null
    // We should call this method any time we create the view, or if we
    // change which detectors are on/off
    //
    // It should figure out which processor to use and then set it in imageProcessor
    // or if we don't want a processor, leave it as null
    fun setupProcessor() {
        val enableFace = binding.enableFaceDetection.isChecked
        val enableMesh = binding.enableMeshDetection.isChecked
        val enableContour = binding.enableContourDetection.isChecked
        val enableSelfie = binding.enableSelfieSegmentation.isChecked
//        if (!enableFace){
//            val nullFace: TextView = binding.numFaces
//        }
        if (enableFace) {
            val options = FaceDetectorOptions.Builder()
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .enableTracking()
                .build()

            imageProcessor = FaceDetectorProcessor(requireContext(), options, binding.numFaces)
        } else if (enableContour) {
            val contourMode = if (enableContour) // change to if contour is on
                FaceDetectorOptions.CONTOUR_MODE_ALL
            else
                FaceDetectorOptions.CONTOUR_MODE_NONE
            Log.d(
                "MeshDebug",
                "Setting up processor with enableMesh: $enableMesh, contourMode: $contourMode"
            )
            val options = FaceDetectorOptions.Builder()
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                .setContourMode(contourMode)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .build()

            imageProcessor = FaceDetectorProcessor(requireContext(), options, binding.numFaces)
        } else if (enableMesh) {
            imageProcessor = FaceMeshDetectorProcessor(requireContext())
        } else if(!enableFace&&!enableMesh&&!enableContour&&!enableSelfie){
            imageProcessor = null
        }
    }

    fun processImage(path : File, graphicOverlay: GraphicOverlay) {
        graphicOverlay.clear()
        setBaseImage(path, graphicOverlay)
        binding.numFaces.setText("")

        setupProcessor()
        val bitmap = rotatedBitmap(path)
        if (imageProcessor != null) {
            imageProcessor!!.processBitmap(bitmap, graphicOverlay)
        } else {
            graphicOverlay.add(CameraImageGraphic(graphicOverlay, bitmap))
        }
    }




    private fun updateUi(crime: Crime) {
        binding.apply {
            if (crimeTitle.text.toString() != crime.title) {
                crimeTitle.setText(crime.title)
            }
            crimeDate.text = crime.date.toString()
            crimeDate.setOnClickListener {
                findNavController().navigate(
                    CrimeDetailFragmentDirections.selectDate(crime.date)
                )
            }

            crimeSolved.isChecked = crime.isSolved

            crimeReport.setOnClickListener {
                val reportIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, getCrimeReport(crime))
                    putExtra(
                        Intent.EXTRA_SUBJECT,
                        getString(R.string.crime_report_subject)
                    )
                }
                val chooserIntent = Intent.createChooser(
                    reportIntent,
                    getString(R.string.send_report)
                )
                startActivity(chooserIntent)
            }

            crimeSuspect.text = crime.suspect.ifEmpty {
                getString(R.string.crime_suspect_text)
            }

            updatePhotos(crime)
        }
    }

    private fun getCrimeReport(crime: Crime): String {
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }

        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()
        val suspectText = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }

        return getString(
            R.string.crime_report,
            crime.title, dateString, solvedString, suspectText
        )
    }

    private fun parseContactSelection(contactUri: Uri) {
        val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)

        val queryCursor = requireActivity().contentResolver
            .query(contactUri, queryFields, null, null, null)

        queryCursor?.use { cursor ->
            if (cursor.moveToFirst()) {
                val suspect = cursor.getString(0)
                crimeDetailViewModel.updateCrime { oldCrime ->
                    oldCrime.copy(suspect = suspect)
                }
            }
        }
    }

    private fun canResolveIntent(intent: Intent): Boolean {
        val packageManager: PackageManager = requireActivity().packageManager
        val resolvedActivity: ResolveInfo? =
            packageManager.resolveActivity(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
        return resolvedActivity != null
    }

    private fun updatePhoto(graphicOverlay: GraphicOverlay, photoFileName: String?) {
        if (graphicOverlay.tag != photoFileName) {
            val photoFile = photoFileName?.let {
                File(requireContext().applicationContext.filesDir, it)
            }

            if (photoFile?.exists() == true && photoFile.isFile) {
                graphicOverlay.doOnLayout {
                    try {
                        graphicOverlay.tag = photoFileName
                        graphicOverlay.contentDescription = getString(R.string.crime_photo_image_description)
                        processImage(photoFile, graphicOverlay)
                    } catch (e: Exception) {
                        Log.e("updatePhoto", "Error updating photo: ${e.localizedMessage}", e)
                        handleNoPhoto(graphicOverlay)
                    }
                }
            } else {
                Log.e("updatePhoto", "File does not exist or is not a file: ${photoFile?.path}")
                handleNoPhoto(graphicOverlay)
            }
        }
    }


    private fun handleNoPhoto(graphicOverlay: GraphicOverlay) {
        graphicOverlay.clear()
        graphicOverlay.tag = null
        graphicOverlay.contentDescription = getString(R.string.crime_photo_no_image_description)
    }



    private fun updatePhotos(crime: Crime) {
        updatePhoto(binding.graphicOverlay, crime.photoFileNames.getOrNull(0))
        updatePhoto(binding.crimePhoto2, crime.photoFileNames.getOrNull(1))
        updatePhoto(binding.crimePhoto3, crime.photoFileNames.getOrNull(2))
        updatePhoto(binding.crimePhoto4, crime.photoFileNames.getOrNull(3))
    }

}
