

import PluginBase = require("plugin/PluginBase");
import { attrToString, pathToString } from "utility/GmeString";
import { getCoreGuid } from "utility/CoreExtras";

const POINTER_SET_DIV = "-";
const CONTAINMENT_PREFIX = "";

/**
     * Get the schema from the nodes having meta rules.
     * https://github.com/webgme/webgme/wiki/GME-Core-API#the-traverse-method
     * sponsor function makes extensive use of a dictionary to build up a tree.
     *
     * @param {GmeClasses.Core}     core        [description]
     * @param {Node}              rootNode    [description]
     * @param {Core.Callback} mainHandler [description]
     */
export function getTreeModel(sponsor: PluginBase, core: GmeClasses.Core,
    _rootNode: Core.Node, _metaNode: Node): void { // Promise<Map<string, string>> {
    // let config: GmeConfig.GmeConfig = sponsor.getCurrentConfig();
    // let configDictionary: Core.Dictionary = config;

    /**
    * Visitor function store.
    */
    let fcoName: string = attrToString(core.getAttribute(core.getFCO(sponsor.rootNode), "name"));
    let languageName: string = attrToString(core.getAttribute(sponsor.rootNode, "name"));
    sponsor.logger.info(`get model tree : ${languageName}:${fcoName}`);
    let rootEntry = new Map<string, string>();
    rootEntry.set("version", "0.0.1");
    /**
     * A dictionary: look up nodes based on their path name.
     */
    let path2entry: GmeCommon.Dictionary<any> = { "": rootEntry };

    /**
     * The base node makes reference to inheritance.
     * The parent node makes reference to containment.
     * The traverse function follows the containment tree.
     * @type {[type]}
     */
    let visitFn = (node: Core.Node, done: GmeCommon.VoidFn): void => {
        let core = sponsor.core;
        // let nodeName = core.getAttribute(node, "name");

        let metaName = (core.isLibraryRoot(node))
            ? ":LibraryRoot:"
            : core.getAttribute(core.getBaseType(node), "name");
        let containRel = `${CONTAINMENT_PREFIX}${metaName}`;
        let sourceEntry: GmeCommon.Dictionary<any> = { "lang": `${languageName}:${containRel}` };
        // let baseNode = core.getBase(node);
        let nodePath = core.getPath(node);
        path2entry[nodePath] = sourceEntry;

        let parent = core.getParent(node);
        if (parent === null) { return; }
        let parentPath = core.getPath(parent);
        let parentData = path2entry[parentPath];
        parentData[containRel] = parentData[containRel] || [];
        parentData[containRel].push(sourceEntry);

        sourceEntry["id"] = getCoreGuid(core, node);
        core.getAttributeNames(node).forEach((attrName: string) => {
            sourceEntry[attrName] = core.getAttribute(node, attrName);
        });

        try {
            let ptrNameList = core.getPointerNames(node);
            ptrNameList.forEach(async (ptrName, _index, _array) => {

                let targetPathRaw = pathToString(core.getPointerPath(node, ptrName));
                if (typeof targetPathRaw !== "string") { return; }
                let targetPath: string = targetPathRaw;
                let targetNode = await core.loadByPath(sponsor.rootNode, targetPath);

                if (ptrName === "base") {
                    sourceEntry[`${ptrName}${POINTER_SET_DIV}${fcoName}`]
                        = getCoreGuid(core, targetNode);
                } else {
                    let targetMetaNode = core.getBaseType(targetNode);
                    let targetMetaName = core.getAttribute(targetMetaNode, "name");
                    sourceEntry[`${ptrName}${POINTER_SET_DIV}${targetMetaName}`]
                        = getCoreGuid(core, targetNode);
                }
            });
        } finally { }

        try {
            // get sets
            let setNameList = core.getSetNames(node);
            setNameList.forEach((setName, _index, _array) => {

                try {
                    let memberPathList = core.getMemberPaths(node, setName);
                    memberPathList.forEach(async (memberPath, _index, _array) => {

                        try {
                            let memberNode = await core.loadByPath(sponsor.rootNode, memberPath);

                            let memberMetaNode = core.getBaseType(memberNode);
                            let memberMetaName = core.getAttribute(memberMetaNode, "name");
                            let setAttr = `${setName}${POINTER_SET_DIV}${memberMetaName}`;

                            sourceEntry[setAttr] = typeof sourceEntry[setAttr] === "string"
                                ? `${sourceEntry[setAttr]} ${core.getGuid(memberNode)}`
                                : getCoreGuid(core, memberNode);
                        } finally { }
                    });
                } finally { }

            });
        } finally {
            done();
        }

        /**
        * Visit the node and perform the function.
        * Documentation for traverse.
        * https://github.com/webgme/webgme/wiki/GME-Core-API#the-traverse-method
        * Related example using traverse.
        * https://github.com/webgme/xmi-tools/blob/master/src/plugins/XMIExporter/XMIExporter.js#L430
        */

    };

    core.traverse(sponsor.rootNode, { excludeRoot: true }, visitFn);
    console.log(`DATA: ${rootEntry}`);
    // return  rootEntry;
}
