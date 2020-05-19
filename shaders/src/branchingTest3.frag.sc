$input v_fragPos, v_normal, v_texcoord0

#include <bgfx_shader.sh>
#include "shaderlib.sh"

//#include "uniforms.sc"


//void main()
//{
//    vec3 result = vec3_splat(0.0); // vec3_splat != vec3 for direct3D

//    vec3 color;
//    if (material_hasDiffuseTexture > -0.5) {
//        color = texture2D(s_diffuse, v_texcoord0).xyz;
//        // color = toLinear(texture2D(s_diffuse, v_texcoord0) ).xyz;
//    }
//    else {
//        color = material_diffuse;
//    }

//    gl_FragColor.xyz = color;
//    gl_FragColor.w = 1.0;
//}
//#define HAS_DIFFUSE_TEXTURE
//#define HAS_SPECULAR_TEXTURE

//#if HAS_DIFFUSE_TEXTURE > 0
#if HAS_ADDITIONAL_TEXTURE_0 > 0
//uniform vec4 hasDiffuseTexture;
SAMPLER2D(diffuseTexture, 0);
#endif
//#if HAS_SPECULAR_TEXTURE > 0
#if HAS_ADDITIONAL_TEXTURE_1 > 0
//uniform vec4 hasSpecularTexture;
SAMPLER2D(specularTexture, 1);
#endif

uniform vec4 diffuseColor;


//#if HAS_ADDITIONAL_TEXTURE_0 > 0
//SAMPLER2D(additionalTexture0, 2);
//#endif
//#if HAS_ADDITIONAL_TEXTURE_1 > 0
//SAMPLER2D(additionalTexture1, 3);
//#endif
#if HAS_ADDITIONAL_TEXTURE_2 > 0
SAMPLER2D(additionalTexture0, 4);
#endif
#if HAS_ADDITIONAL_TEXTURE_3 > 0
SAMPLER2D(additionalTexture1, 5);
#endif
#if HAS_ADDITIONAL_TEXTURE_4 > 0
SAMPLER2D(additionalTexture2, 6);
#endif
#if HAS_ADDITIONAL_TEXTURE_5 > 0
SAMPLER2D(additionalTexture3, 7);
#endif
#if HAS_ADDITIONAL_TEXTURE_6 > 0
SAMPLER2D(additionalTexture4, 8);
#endif
#if HAS_ADDITIONAL_TEXTURE_7 > 0
SAMPLER2D(additionalTexture5, 9);
#endif

//uniform vec4 nAdditionalTexture;
//#define N_ADDITIONAL_TEXTURE

void main() {

    vec4 diffColor;
    vec4 specColor;

//    if (hasDiffuseTexture.x > 0.5) {
//#if HAS_DIFFUSE_TEXTURE > 0
#if HAS_ADDITIONAL_TEXTURE_0 > 0
        diffColor = texture2D(diffuseTexture, v_texcoord0) * vec4(diffuseColor.xyz, 1.0);
#else
//    }
//    else {
        diffColor = vec4(diffuseColor.xyz, 1.0);
//    }
#endif

//#if HAS_SPECULAR_TEXTURE > 0
#if HAS_ADDITIONAL_TEXTURE_1 > 0
//    if (hasSpecularTexture.x > 0.5) {
        specColor = texture2D(specularTexture, v_texcoord0);
#else
//    }
//    else {
        specColor = vec4(0.0, 0.0, 0.0, 1.0);
//    }
#endif

#if HAS_ADDITIONAL_TEXTURE_2 > 0
            specColor = (texture2D(additionalTexture0, v_texcoord0) + specColor);
#endif
#if HAS_ADDITIONAL_TEXTURE_3 > 0
            specColor = (texture2D(additionalTexture1, v_texcoord0) + specColor);
#endif
#if HAS_ADDITIONAL_TEXTURE_4 > 0
            specColor = (texture2D(additionalTexture2, v_texcoord0) + specColor);
#endif
#if HAS_ADDITIONAL_TEXTURE_5 > 0
            specColor = (texture2D(additionalTexture3, v_texcoord0) + specColor);
#endif
#if HAS_ADDITIONAL_TEXTURE_6 > 0
            specColor = (texture2D(additionalTexture4, v_texcoord0) + specColor);
#endif
#if HAS_ADDITIONAL_TEXTURE_7 > 0
            specColor = (texture2D(additionalTexture5, v_texcoord0) + specColor);
#endif

#ifdef N_ADDITIONAL_TEXTURE
    specColor /= N_ADDITIONAL_TEXTURE;
#endif

    gl_FragColor = diffColor + specColor;
    gl_FragColor.w = 1.0;
}
